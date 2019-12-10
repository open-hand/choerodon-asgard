package io.choerodon.asgard.app.service.impl;

import java.io.IOException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import io.choerodon.asgard.api.vo.*;
import io.choerodon.asgard.infra.enums.TriggerType;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import io.choerodon.asgard.infra.dto.*;
import io.choerodon.asgard.app.service.NoticeService;
import io.choerodon.asgard.app.service.QuartzJobService;
import io.choerodon.asgard.app.service.ScheduleTaskService;
import io.choerodon.asgard.infra.enums.DefaultAutowiredField;
import io.choerodon.asgard.infra.enums.MemberType;
import io.choerodon.asgard.infra.feign.IamFeignClient;
import io.choerodon.asgard.infra.mapper.QuartzMethodMapper;
import io.choerodon.asgard.infra.mapper.QuartzTaskInstanceMapper;
import io.choerodon.asgard.infra.mapper.QuartzTaskMapper;
import io.choerodon.asgard.infra.mapper.QuartzTaskMemberMapper;
import io.choerodon.asgard.infra.utils.CommonUtils;
import io.choerodon.asgard.infra.utils.ConvertUtils;
import io.choerodon.asgard.infra.utils.TriggerUtils;
import io.choerodon.asgard.property.PropertyJobParam;
import io.choerodon.asgard.property.PropertyTimedTask;
import io.choerodon.asgard.schedule.ParamType;
import io.choerodon.asgard.schedule.QuartzDefinition;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.core.oauth.DetailsHelper;

@Service
public class ScheduleTaskServiceImpl implements ScheduleTaskService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleTaskService.class);

    private static final String TASK_NOT_EXIST = "error.scheduleTask.taskNotExist";

    private static final String LEVEL_NOT_MATCH = "error.scheduleTask.levelNotMatch";

    private static final String SOURCE_ID_NOT_MATCH = "error.scheduleTask.sourceIdNotMatch";

    private final ModelMapper modelMapper = new ModelMapper();

    private final ObjectMapper objectMapper = new ObjectMapper();

    private QuartzMethodMapper methodMapper;

    private QuartzTaskMapper taskMapper;

    private QuartzJobService quartzJobService;

    private QuartzTaskInstanceMapper instanceMapper;

    private IamFeignClient iamFeignClient;

    private NoticeService noticeService;

    private QuartzTaskMemberMapper quartzTaskMemberMapper;


    public ScheduleTaskServiceImpl(QuartzMethodMapper methodMapper,
                                   QuartzTaskMapper taskMapper,
                                   QuartzJobService quartzJobService,
                                   QuartzTaskInstanceMapper instanceMapper,
                                   QuartzTaskMemberMapper quartzTaskMemberMapper,
                                   IamFeignClient iamFeignClient,
                                   NoticeService noticeService) {
        this.methodMapper = methodMapper;
        this.taskMapper = taskMapper;
        this.quartzJobService = quartzJobService;
        this.instanceMapper = instanceMapper;
        this.quartzTaskMemberMapper = quartzTaskMemberMapper;
        this.iamFeignClient = iamFeignClient;
        this.noticeService = noticeService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QuartzTaskDTO create(final ScheduleTask dto, String level, Long sourceId) {
        if (dto.getStartTime() == null && dto.getStartTimeStr() != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            ParsePosition pos = new ParsePosition(0);
            Date strToDate = formatter.parse(dto.getStartTimeStr(), pos);
            dto.setStartTime(strToDate);
        }
        QuartzTaskDTO quartzTask = modelMapper.map(dto, QuartzTaskDTO.class);
        QuartzMethodDTO method = methodMapper.selectByPrimaryKey(dto.getMethodId());
        validatorLevelAndQuartzMethod(level, method);
        try {
            List<PropertyJobParam> propertyJobParams = objectMapper.readValue(method.getParams(), new TypeReference<List<PropertyJobParam>>() {
            });
            putDefaultParameter(propertyJobParams, dto, level, sourceId);
            quartzTask.setUserDetails(CommonUtils.getUserDetailsJson(objectMapper));
            quartzTask.setExecuteMethod(method.getCode());
            quartzTask.setId(null);
            if (quartzTask.getStatus() == null || quartzTask.getStatus().equals("")) {
                quartzTask.setStatus(QuartzDefinition.TaskStatus.ENABLE.name());
            }
            quartzTask.setExecuteParams(objectMapper.writeValueAsString(dto.getParams()));
            quartzTask.setLevel(level);
            quartzTask.setSourceId(sourceId);
            quartzTask.setExecuteStrategy(dto.getExecuteStrategy());
            validExecuteParams(dto.getParams(), propertyJobParams);
            if (taskMapper.insertSelective(quartzTask) != 1) {
                throw new CommonException("error.scheduleTask.create");
            }
            QuartzTaskDTO db = taskMapper.selectByPrimaryKey(quartzTask.getId());
            //插入通知对象失败需要回滚
            List<QuartzTaskMemberDTO> noticeMembers = insertNoticeMember(dto, level, quartzTask);
            //发送通知失败不需要回滚,已捕获异常
            noticeService.sendNotice(quartzTask, noticeMembers, "启用");
            quartzJobService.addJob(db);
            LOGGER.info("create job: {}", quartzTask);
            return db;
        } catch (IOException e) {
            throw new CommonException("error.scheduleTask.createJsonIOException", e);
        }
    }

    /**
     * 插入通知成员信息
     *
     * @param dto
     * @param level
     * @param quartzTask
     * @return
     */
    private List<QuartzTaskMemberDTO> insertNoticeMember(final ScheduleTask dto, final String level, final QuartzTaskDTO quartzTask) {
        List<QuartzTaskMemberDTO> quartzTaskMembers = new ArrayList<>();
        if (dto.getNotifyUser() == null) {
            return quartzTaskMembers;
        }
        //判断是否为空，为了单测
        Long currentUserId = DetailsHelper.getUserDetails() != null ? DetailsHelper.getUserDetails().getUserId() : null;
        if (dto.getNotifyUser().getAdministrator()) {
            Role role = getAdministratorRoleByLevel(level);
            quartzTaskMembers.add(insertMember(quartzTask.getId(), MemberType.ROLE, role.getId()));
        }
        if (dto.getNotifyUser().getCreator()) {
            quartzTaskMembers.add(insertMember(quartzTask.getId(), MemberType.CREATOR, currentUserId));
        }
        Long[] assignUserIds = dto.getAssignUserIds();
        if (dto.getNotifyUser().getAssigner() && assignUserIds != null) {
            Set<Long> ids = Arrays.stream(assignUserIds).collect(Collectors.toSet());
            if (dto.getNotifyUser().getCreator()) {
                ids.remove(currentUserId);
            }
            ids.forEach(id -> quartzTaskMembers.add(insertMember(quartzTask.getId(), MemberType.ASSIGNER, id)));
        }
        return quartzTaskMembers;
    }


    private QuartzTaskMemberDTO insertMember(final Long quartzTaskId, final MemberType user, final Long userId) {
        QuartzTaskMemberDTO quartzTaskMember = new QuartzTaskMemberDTO();
        quartzTaskMember.setTaskId(quartzTaskId);
        quartzTaskMember.setMemberType(user.value());
        quartzTaskMember.setMemberId(userId);
        if (quartzTaskMemberMapper.insert(quartzTaskMember) != 1) {
            throw new CommonException("error.quartzTaskMember.create");
        }
        return quartzTaskMember;
    }

    /**
     * 通过层级得到对应的Administrator的RoleDTO
     *
     * @param level
     * @return
     */
    private Role getAdministratorRoleByLevel(String level) {
        Role role = new Role();
        if (ResourceLevel.SITE.value().equals(level)) {
            role = iamFeignClient.queryByCode(InitRoleCode.SITE_ADMINISTRATOR).getBody();
        }
        if (ResourceLevel.ORGANIZATION.value().equals(level)) {
            role = iamFeignClient.queryByCode(InitRoleCode.ORGANIZATION_ADMINISTRATOR).getBody();
        }
        if (ResourceLevel.PROJECT.value().equals(level)) {
            role = iamFeignClient.queryByCode(InitRoleCode.PROJECT_ADMINISTRATOR).getBody();
        }
        return role;
    }

    /**
     * 如果propertyJobParams中有默认自动注入参数，那么会自动注入到dto中
     */
    private void putDefaultParameter(final List<PropertyJobParam> propertyJobParams, ScheduleTask dto, final String level, final Long sourceId) {
        Map<String, Object> params = dto.getParams();
        Set<String> jobParamsName = propertyJobParams.stream().map(PropertyJobParam::getName).collect(Collectors.toSet());
        //确定params中有需要默认注入的字段才发起feign请求
        if (ResourceLevel.ORGANIZATION.value().equals(level)
                && Arrays.stream(DefaultAutowiredField.organizationDefaultField()).anyMatch(jobParamsName::contains)) {
            Organization organization = iamFeignClient.queryOrganization(sourceId).getBody();
            params.put(DefaultAutowiredField.ORGANIZATION_ID, organization.getId());
            params.put(DefaultAutowiredField.ORGANIZATION_NAME, organization.getName());
            params.put(DefaultAutowiredField.ORGANIZATION_CODE, organization.getCode());
        }
        if (ResourceLevel.PROJECT.value().equals(level)
                && Arrays.stream(DefaultAutowiredField.projectDefaultField()).anyMatch(jobParamsName::contains)) {
            Project project = iamFeignClient.queryProject(sourceId).getBody();
            params.put(DefaultAutowiredField.PROJECT_ID, project.getId());
            params.put(DefaultAutowiredField.PROJECT_NAME, project.getName());
            params.put(DefaultAutowiredField.PROJECT_CODE, project.getCode());
        }
        dto.setParams(params);
    }

    private void validatorLevelAndQuartzMethod(String level, QuartzMethodDTO method) {
        if (method == null) {
            throw new CommonException("error.scheduleTask.methodNotExist");
        }
        if (!level.equals(method.getLevel())) {
            throw new CommonException(LEVEL_NOT_MATCH);
        }
    }

    private void validExecuteParams(final Map<String, Object> params, final List<PropertyJobParam> paramDefinitionList) {
        params.forEach((k, v) -> {
            PropertyJobParam jobParam = getPropertyJobParam(k, paramDefinitionList);
            if (jobParam != null && !validExecuteParam(v, jobParam.getType(), jobParam.getDefaultValue())) {
                throw new CommonException("error.scheduleTask.paramInvalidType");
            }
        });
    }

    private PropertyJobParam getPropertyJobParam(final String key, final List<PropertyJobParam> paramDefinitionList) {
        for (PropertyJobParam propertyJobParam : paramDefinitionList) {
            if (key.equals(propertyJobParam.getName())) {
                return propertyJobParam;
            }
        }
        return null;
    }

    private boolean validExecuteParam(final Object value, final String type, final Object defaultValue) {
        if (value == null) {
            return defaultValue != null;
        }
        ParamType paramType = ParamType.getParamTypeByValue(type);
        if (paramType == null) {
            throw new CommonException("error.scheduleTask.paramType");
        }
        switch (paramType) {
            case INTEGER:
                return value.getClass().equals(Integer.class);
            case LONG:
                return value.getClass().equals(Long.class) || value.getClass().equals(Integer.class);
            case STRING:
                return value.getClass().equals(String.class);
            case BOOLEAN:
                return value.getClass().equals(Boolean.class);
            case DOUBLE:
                return value.getClass().equals(Double.class);
            default:
                return false;
        }
    }

    @Transactional
    @Override
    public void enable(long id, long objectVersionNumber, String level, Long sourceId) {
        QuartzTaskDTO quartzTask = getQuartzTask(id, level, sourceId);
        //将 停用的任务 启用，并恢复job
        if (QuartzDefinition.TaskStatus.DISABLE.name().equals(quartzTask.getStatus())) {
            quartzTask.setStatus(QuartzDefinition.TaskStatus.ENABLE.name());
            quartzTask.setObjectVersionNumber(objectVersionNumber);
            if (taskMapper.updateByPrimaryKey(quartzTask) != 1) {
                throw new CommonException("error.scheduleTask.enableTaskFailed");
            }
            //更新下次执行时间
            QuartzTaskDTO task = taskMapper.selectByPrimaryKey(id);
            QuartzTaskInstanceDTO lastInstance = instanceMapper.selectLastInstance(id);
            if (lastInstance != null) {
                QuartzTaskInstanceDTO instance = new QuartzTaskInstanceDTO();
                instance.setId(lastInstance.getId());
                instance.setObjectVersionNumber(lastInstance.getObjectVersionNumber());
                if (quartzTask.getTriggerType().equalsIgnoreCase(TriggerType.CRON.getValue())) {
                    instance.setPlannedNextTime(TriggerUtils.getStartTime(task.getCronExpression()));
                } else {
                    instance.setPlannedNextTime(new Date());
                }
                if (instanceMapper.updateByPrimaryKeySelective(instance) != 1) {
                    throw new CommonException("error.scheduleTask.enableTask.update.next.time.Failed");
                }
            }
            quartzJobService.resumeJob(id);
            List<QuartzTaskMemberDTO> noticeMembers = getQuartzTaskMembersByTaskId(quartzTask.getId());
            noticeService.sendNotice(quartzTask, noticeMembers, "启用");
            LOGGER.info("enable job: {}", quartzTask);
        }
    }

    /**
     * 通过taskId查询QuartzTaskMember
     *
     * @param taskId
     * @return
     */
    private List<QuartzTaskMemberDTO> getQuartzTaskMembersByTaskId(final Long taskId) {
        QuartzTaskMemberDTO query = new QuartzTaskMemberDTO();
        query.setTaskId(taskId);
        return quartzTaskMemberMapper.select(query);
    }

    @Override
    public QuartzTaskDTO getQuartzTask(long id, String level, Long sourceId) {
        QuartzTaskDTO quartzTask = taskMapper.selectByPrimaryKey(id);
        if (quartzTask == null) {
            throw new CommonException(TASK_NOT_EXIST);
        }
        //不是当前源的任务
        if (!sourceId.equals(quartzTask.getSourceId())) {
            throw new CommonException(SOURCE_ID_NOT_MATCH);
        }
        if (!level.equals(quartzTask.getLevel())) {
            throw new CommonException(LEVEL_NOT_MATCH);
        }
        return quartzTask;
    }

    @Transactional
    @Override
    public void disable(long id, Long objectVersionNumber, boolean executeWithIn) {
        QuartzTaskDTO quartzTask = taskMapper.selectByPrimaryKey(id);
        if (quartzTask == null) {
            throw new CommonException(TASK_NOT_EXIST);
        }
        if (executeWithIn) {
            objectVersionNumber = quartzTask.getObjectVersionNumber();
        }
        disableTaskAndPauseJob(id, objectVersionNumber, quartzTask);
    }

    private void disableTaskAndPauseJob(long id, Long objectVersionNumber, QuartzTaskDTO quartzTask) {
        // 将 未结束的任务 置为 停止，并暂停job
        if (QuartzDefinition.TaskStatus.ENABLE.name().equals(quartzTask.getStatus())) {
            quartzTask.setStatus(QuartzDefinition.TaskStatus.DISABLE.name());
            quartzTask.setObjectVersionNumber(objectVersionNumber);
            if (taskMapper.updateByPrimaryKey(quartzTask) != 1) {
                throw new CommonException("error.scheduleTask.disableTaskFailed");
            }
            quartzJobService.pauseJob(id);
            List<QuartzTaskMemberDTO> noticeMembers = getQuartzTaskMembersByTaskId(quartzTask.getId());
            noticeService.sendNotice(quartzTask, noticeMembers, "禁用");
            LOGGER.info("disable job: {}", quartzTask);
        }
    }

    @Override
    public void disableByLevelAndSourceId(String level, long sourceId) {
        QuartzTaskDTO query = new QuartzTaskDTO();
        query.setLevel(level);
        query.setSourceId(sourceId);
        List<QuartzTaskDTO> quartzTasks = taskMapper.select(query);
        quartzTasks.forEach(quartzTask -> disableTaskAndPauseJob(quartzTask.getId(), quartzTask.getObjectVersionNumber(), quartzTask));
    }

    @Transactional
    @Override
    public void delete(long id, String level, Long sourceId) {
        QuartzTaskDTO quartzTask = getQuartzTask(id, level, sourceId);
        if (taskMapper.deleteByPrimaryKey(id) != 1) {
            throw new CommonException("error.scheduleTask.deleteTaskFailed");
        }
        quartzJobService.removeJob(id);
        LOGGER.info("delete job: {}", quartzTask);
    }

    @Override
    public ResponseEntity<PageInfo<QuartzTask>> pageQuery(Pageable pageable, String status, String name, String description, String params, String level, Long sourceId) {
        PageInfo<QuartzTaskDTO> result = PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize()).doSelectPageInfo(() -> taskMapper.fulltextSearch(status, name, description, params, level, sourceId));
        List<QuartzTaskDTO> quartzTasks = result.getList();
        Page<QuartzTask> resultPage = new Page<>(pageable.getPageNumber(), pageable.getPageSize());
        resultPage.setTotal(result.getTotal());
        List<QuartzTask> quartzTaskList = new ArrayList<>();
        quartzTasks.forEach(q -> {
            Date lastStartTime = null;
            Date nextStartTime;
            QuartzTaskInstanceDTO lastInstance = instanceMapper.selectLastInstance(q.getId());
            if (lastInstance != null) {
                lastStartTime = lastInstance.getActualStartTime();
                nextStartTime = lastInstance.getPlannedNextTime();
            } else {
                // 初次执行 开始时间已过，TriggerType 为 Cron ，则设当前时间的最近执行时间为下次执行时间 ；否则 设 开始时间 为 下次执行时间
                if (q.getStartTime().getTime() < new Date().getTime() && TriggerType.CRON.getValue().equals(q.getTriggerType())) {
                    nextStartTime = TriggerUtils.getStartTime(q.getCronExpression());
                } else {
                    nextStartTime = q.getStartTime();
                }
            }
            if (!QuartzDefinition.TaskStatus.ENABLE.name().equals(q.getStatus())) {
                nextStartTime = null;
            }
            quartzTaskList.add(new QuartzTask(q.getId(), q.getName(), q.getDescription(), lastStartTime, nextStartTime, q.getStatus(), q.getObjectVersionNumber()));
        });
        resultPage.addAll(quartzTaskList);
        return new ResponseEntity<>(resultPage.toPageInfo(), HttpStatus.OK);
    }

    @Override
    public void finish(long id) {
        QuartzTaskDTO quartzTask = taskMapper.selectByPrimaryKey(id);
        if (quartzTask == null) {
            LOGGER.warn("finish job error, quartzTask is not exist {}", id);
        } else {
            quartzTask.setStatus(QuartzDefinition.TaskStatus.FINISHED.name());
            if (taskMapper.updateByPrimaryKey(quartzTask) == 1) {
                LOGGER.info("finish job: {}", quartzTask);
            } else {
                LOGGER.error("finish job error, updateStatus failed : {}", quartzTask);
            }
            List<QuartzTaskMemberDTO> noticeMembers = getQuartzTaskMembersByTaskId(quartzTask.getId());
            noticeService.sendNotice(quartzTask, noticeMembers, "结束");
        }
    }

    @Override
    public ScheduleTaskDetail getTaskDetail(Long id, String level, Long sourceId) {
        QuartzTaskDTO quartzTask = getQuartzTask(id, level, sourceId);
        Date lastStartTime = null;
        Date nextStartTime;
        QuartzTaskInstanceDTO lastInstance = instanceMapper.selectLastInstance(id);
        if (lastInstance != null) {
            lastStartTime = lastInstance.getActualStartTime();
            nextStartTime = lastInstance.getPlannedNextTime();
        } else {
            // 初次执行 开始时间已过，TriggerType 为 Cron ，则设当前时间的最近执行时间为下次执行时间 ；否则 设 开始时间 为 下次执行时间
            if (quartzTask.getStartTime().getTime() < new Date().getTime() && TriggerType.CRON.getValue().equals(quartzTask.getTriggerType())) {
                nextStartTime = TriggerUtils.getStartTime(quartzTask.getCronExpression());
            } else {
                nextStartTime = quartzTask.getStartTime();
            }
        }
        if (!QuartzDefinition.TaskStatus.ENABLE.name().equals(quartzTask.getStatus())) {
            nextStartTime = null;
        }
        QuartzTaskDetail quartzTaskDetail = taskMapper.selectTaskById(id);
        //得到通知对象
        List<QuartzTaskMemberDTO> members = getQuartzTaskMembersByTaskId(quartzTask.getId());
        ScheduleTaskDetail detailDTO = new ScheduleTaskDetail(quartzTaskDetail, objectMapper, lastStartTime, nextStartTime);
        detailDTO.setNotifyUser(getNotifyUser(members));
        detailDTO.setExecuteStrategy(quartzTaskDetail.getExecuteStrategy());
        //设置方法描述
        QuartzMethodDTO query = new QuartzMethodDTO();
        query.setCode(detailDTO.getMethodCode());
        QuartzMethodDTO quartzMethod = methodMapper.selectOne(query);
        detailDTO.setMethodDescription(Optional.ofNullable(quartzMethod).map(QuartzMethodDTO::getDescription).orElse(null));
        detailDTO.setObjectVersionNumber(quartzTask.getObjectVersionNumber());
        return detailDTO;
    }

    /**
     * 得到通知对象NotifyUser
     *
     * @param members
     * @return
     */
    private ScheduleTaskDetail.NotifyUser getNotifyUser(List<QuartzTaskMemberDTO> members) {
        if (members == null || members.isEmpty()) return null;
        ScheduleTaskDetail.User creator = null;
        Boolean administrator = false;
        List<Long> idList = new LinkedList<>();
        List<ScheduleTaskDetail.User> assigners = new LinkedList<>();
        for (int i = 0; i < members.size(); i++) {
            QuartzTaskMemberDTO member = members.get(i);
            if (MemberType.ASSIGNER.value().equals(member.getMemberType())) {
                idList.add(member.getMemberId());
            } else if (MemberType.CREATOR.value().equals(member.getMemberType())) {
                Long[] creatorId = new Long[1];
                creatorId[0] = member.getMemberId();
                List<User> users = iamFeignClient.listUsersByIds(creatorId).getBody();
                if (!users.isEmpty()) {
                    User user = users.get(0);
                    creator = new ScheduleTaskDetail.User(user.getLoginName(), user.getRealName());
                }
            } else if (MemberType.ROLE.value().equals(member.getMemberType())) {
                administrator = true;
            }
        }
        if (!idList.isEmpty()) {
            List<User> users = iamFeignClient.listUsersByIds(idList.toArray(new Long[0])).getBody();
            for (int i = 0; i < users.size(); i++) {
                User userDTO = users.get(i);
                ScheduleTaskDetail.User user = new ScheduleTaskDetail.User(userDTO.getLoginName(), userDTO.getRealName());
                assigners.add(user);
            }
        }
        return new ScheduleTaskDetail.NotifyUser(creator, administrator, assigners);
    }

    @Override
    public void checkName(String name, String level, Long sourceId) {
        List<Long> ids = taskMapper.selectTaskIdByName(name, level, sourceId);
        if (!ids.isEmpty()) {
            throw new CommonException("error.scheduleTask.name.exist");
        }
    }

    @Override
    public void checkNameAllLevel(String name) {
        QuartzTaskDTO task = new QuartzTaskDTO();
        task.setName(name);
        List<QuartzTaskDTO> select = taskMapper.select(task);
        if (!CollectionUtils.isEmpty(select)) {
            throw new CommonException("error.scheduleTask.name.exist");
        }
    }

    @Override
    public void createTaskList(String service, List<PropertyTimedTask> scanTasks, String version) {
        //此处借用闲置属性cronexpression设置是否为一次执行：是返回"1",多次执行返回"0"
        List<QuartzTaskDTO> collect = scanTasks.stream().map(t -> ConvertUtils.convertQuartzTask(objectMapper, t)).collect(Collectors.toList());
        collect.forEach(i -> {
            QuartzMethodDTO method = getQuartzMethod(i);
            QuartzTaskDTO query = new QuartzTaskDTO();
            query.setExecuteMethod(i.getExecuteMethod());
            List<QuartzTaskDTO> dbTasks = taskMapper.select(query);
            //若数据库中无相同任务名  或  数据库中有相同任务名的任务 且 每次部署执行，则 创建task（name=name+version），创建job
            Boolean byTaskName = findByTaskName(dbTasks, i.getName());
            if (!byTaskName || i.getCronExpression().equals("0")) {
                i.setName(i.getName() + ":" + version);
                //若为 同一版本下的相同任务名的 任务，不创建，不执行
                List<QuartzTaskDTO> ifEqual = dbTasks.stream().filter(t -> t.getName().equals(i.getName())).collect(Collectors.toList());
                if (ifEqual.isEmpty()) {
                    try {
                        validExecuteParams(objectMapper.readValue(i.getExecuteParams(), new TypeReference<Map<String, Object>>() {
                        }), objectMapper.readValue(method.getParams(), new TypeReference<List<PropertyJobParam>>() {
                        }));
                        if (taskMapper.insertSelective(i) != 1) {
                            throw new CommonException("error.scheduleTask.create");
                        }
                        QuartzTaskDTO db = taskMapper.selectByPrimaryKey(i.getId());
                        quartzJobService.addJob(db);
                        LOGGER.info("create job: {}", i);
                    } catch (IOException e) {
                        throw new CommonException("error.scheduleTask.createJsonIOException", e);
                    }
                }
            }
        });
    }

    private QuartzMethodDTO getQuartzMethod(QuartzTaskDTO i) {
        QuartzMethodDTO method = new QuartzMethodDTO();
        method.setCode(i.getExecuteMethod());
        List<QuartzMethodDTO> select = methodMapper.select(method);
        if (select.isEmpty()) {
            throw new CommonException("error.scheduleTask.methodNotExist");
        } else if (select.size() == 1) {
            method = select.get(0);
        }
        return method;
    }


    private Boolean findByTaskName(final List<QuartzTaskDTO> tasks, final String taskName) {
        for (QuartzTaskDTO task : tasks) {
            if (task.getName().contains(":")) {
                String[] strings = StringUtils.delimitedListToStringArray(task.getName(), ":");
                if (strings[0].equals(taskName)) {
                    return true;
                }
            }
        }
        return false;
    }
}
