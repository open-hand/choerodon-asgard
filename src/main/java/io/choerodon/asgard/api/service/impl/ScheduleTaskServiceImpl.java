package io.choerodon.asgard.api.service.impl;

import static io.choerodon.asgard.api.service.impl.SystemNotificationServiceImpl.ORG_NOTIFICATION_CODE;
import static io.choerodon.asgard.api.service.impl.SystemNotificationServiceImpl.SITE_NOTIFICATION_CODE;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import io.choerodon.asgard.api.dto.*;
import io.choerodon.asgard.api.service.NoticeService;
import io.choerodon.asgard.api.service.QuartzJobService;
import io.choerodon.asgard.api.service.ScheduleTaskService;
import io.choerodon.asgard.domain.*;
import io.choerodon.asgard.infra.enums.DefaultAutowiredField;
import io.choerodon.asgard.infra.enums.MemberType;
import io.choerodon.asgard.infra.feign.IamFeignClient;
import io.choerodon.asgard.infra.mapper.QuartzMethodMapper;
import io.choerodon.asgard.infra.mapper.QuartzTaskInstanceMapper;
import io.choerodon.asgard.infra.mapper.QuartzTaskMapper;
import io.choerodon.asgard.infra.mapper.QuartzTaskMemberMapper;
import io.choerodon.asgard.infra.utils.ConvertUtils;
import io.choerodon.asgard.infra.utils.TriggerUtils;
import io.choerodon.asgard.property.PropertyJobParam;
import io.choerodon.asgard.property.PropertyTimedTask;
import io.choerodon.asgard.schedule.ParamType;
import io.choerodon.asgard.schedule.QuartzDefinition;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

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
    @Transactional
    public QuartzTask create(final ScheduleTaskDTO dto, String level, Long sourceId) {
        QuartzTask quartzTask = modelMapper.map(dto, QuartzTask.class);
        QuartzMethod method = methodMapper.selectByPrimaryKey(dto.getMethodId());
        validatorLevelAndQuartzMethod(level, method);
        try {
            List<PropertyJobParam> propertyJobParams = objectMapper.readValue(method.getParams(), new TypeReference<List<PropertyJobParam>>() {
            });
            putDefaultParameter(propertyJobParams, dto, level, sourceId);
            quartzTask.setExecuteMethod(method.getCode());
            quartzTask.setId(null);
            quartzTask.setStatus(QuartzDefinition.TaskStatus.ENABLE.name());
            quartzTask.setExecuteParams(objectMapper.writeValueAsString(dto.getParams()));
            quartzTask.setLevel(level);
            quartzTask.setSourceId(sourceId);
            validExecuteParams(dto.getParams(), propertyJobParams);
            if (taskMapper.insertSelective(quartzTask) != 1) {
                throw new CommonException("error.scheduleTask.create");
            }
            QuartzTask db = taskMapper.selectByPrimaryKey(quartzTask.getId());
            //插入通知对象失败需要回滚
            List<QuartzTaskMember> noticeMembers = insertNoticeMember(dto, level, quartzTask);
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
    private List<QuartzTaskMember> insertNoticeMember(final ScheduleTaskDTO dto, final String level, final QuartzTask quartzTask) {
        List<QuartzTaskMember> quartzTaskMembers = new ArrayList<>();
        if (dto.getNotifyUser() == null) return quartzTaskMembers;
        //判断是否为空，为了单测
        Long currentUserId = DetailsHelper.getUserDetails() != null ? DetailsHelper.getUserDetails().getUserId() : null;
        if (dto.getNotifyUser().getAdministrator()) {
            RoleDTO roleDTO = getAdministratorRoleByLevel(level);
            quartzTaskMembers.add(insertMember(quartzTask.getId(), MemberType.ROLE, roleDTO.getId()));
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


    private QuartzTaskMember insertMember(final Long quartzTaskId, final MemberType user, final Long userId) {
        QuartzTaskMember quartzTaskMember = new QuartzTaskMember();
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
    private RoleDTO getAdministratorRoleByLevel(String level) {
        RoleDTO roleDTO = new RoleDTO();
        if (ResourceLevel.SITE.value().equals(level)) {
            roleDTO = iamFeignClient.queryByCode(InitRoleCode.SITE_ADMINISTRATOR).getBody();
        }
        if (ResourceLevel.ORGANIZATION.value().equals(level)) {
            roleDTO = iamFeignClient.queryByCode(InitRoleCode.ORGANIZATION_ADMINISTRATOR).getBody();
        }
        if (ResourceLevel.PROJECT.value().equals(level)) {
            roleDTO = iamFeignClient.queryByCode(InitRoleCode.PROJECT_ADMINISTRATOR).getBody();
        }
        return roleDTO;
    }

    /**
     * 如果propertyJobParams中有默认自动注入参数，那么会自动注入到dto中
     *
     * @param dto
     * @param level
     * @param sourceId
     */
    private void putDefaultParameter(final List<PropertyJobParam> propertyJobParams, ScheduleTaskDTO dto, final String level, final Long sourceId) {
        Map<String, Object> params = dto.getParams();
        Set<String> jobParamsName = propertyJobParams.stream().map(PropertyJobParam::getName).collect(Collectors.toSet());
        //确定params中有需要默认注入的字段才发起feign请求
        if (ResourceLevel.ORGANIZATION.value().equals(level)
                && Arrays.stream(DefaultAutowiredField.organizationDefaultField()).anyMatch(jobParamsName::contains)) {
            OrganizationDTO organizationDTO = iamFeignClient.queryOrganization(sourceId).getBody();
            params.put(DefaultAutowiredField.ORGANIZATION_ID, organizationDTO.getId());
            params.put(DefaultAutowiredField.ORGANIZATION_NAME, organizationDTO.getName());
            params.put(DefaultAutowiredField.ORGANIZATION_CODE, organizationDTO.getCode());
        }
        if (ResourceLevel.PROJECT.value().equals(level)
                && Arrays.stream(DefaultAutowiredField.projectDefaultField()).anyMatch(jobParamsName::contains)) {
            ProjectDTO projectDTO = iamFeignClient.queryProject(sourceId).getBody();
            params.put(DefaultAutowiredField.PROJECT_ID, projectDTO.getId());
            params.put(DefaultAutowiredField.PROJECT_NAME, projectDTO.getName());
            params.put(DefaultAutowiredField.PROJECT_CODE, projectDTO.getCode());
        }
        dto.setParams(params);
    }

    private void validatorLevelAndQuartzMethod(String level, QuartzMethod method) {
        if (method == null) {
            throw new CommonException("error.scheduleTask.methodNotExist");
        }
        if (!level.equals(method.getLevel())) {
            throw new CommonException(LEVEL_NOT_MATCH);
        }
    }

    private void validExecuteParams(final Map<String, Object> params, final List<PropertyJobParam> paramDefinitionList) throws IOException {
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
                return value.getClass().equals(Long.class);
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
        QuartzTask quartzTask = getQuartzTask(id, level, sourceId);
        //将 停用的任务 启用，并恢复job
        if (QuartzDefinition.TaskStatus.DISABLE.name().equals(quartzTask.getStatus())) {
            quartzTask.setStatus(QuartzDefinition.TaskStatus.ENABLE.name());
            quartzTask.setObjectVersionNumber(objectVersionNumber);
            if (taskMapper.updateByPrimaryKey(quartzTask) != 1) {
                throw new CommonException("error.scheduleTask.enableTaskFailed");
            }
            quartzJobService.resumeJob(id);
            List<QuartzTaskMember> noticeMembers = getQuartzTaskMembersByTaskId(quartzTask.getId());
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
    private List<QuartzTaskMember> getQuartzTaskMembersByTaskId(final Long taskId) {
        QuartzTaskMember query = new QuartzTaskMember();
        query.setTaskId(taskId);
        return quartzTaskMemberMapper.select(query);
    }

    @Override
    public QuartzTask getQuartzTask(long id, String level, Long sourceId) {
        QuartzTask quartzTask = taskMapper.selectByPrimaryKey(id);
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
        QuartzTask quartzTask = taskMapper.selectByPrimaryKey(id);
        if (quartzTask == null) {
            throw new CommonException(TASK_NOT_EXIST);
        }
        if (executeWithIn) {
            objectVersionNumber = quartzTask.getObjectVersionNumber();
        }
        disableTaskAndPauseJob(id, objectVersionNumber, quartzTask);
    }

    private void disableTaskAndPauseJob(long id, Long objectVersionNumber, QuartzTask quartzTask) {
        // 将 未结束的任务 置为 停止，并暂停job
        if (QuartzDefinition.TaskStatus.ENABLE.name().equals(quartzTask.getStatus())) {
            quartzTask.setStatus(QuartzDefinition.TaskStatus.DISABLE.name());
            quartzTask.setObjectVersionNumber(objectVersionNumber);
            if (taskMapper.updateByPrimaryKey(quartzTask) != 1) {
                throw new CommonException("error.scheduleTask.disableTaskFailed");
            }
            quartzJobService.pauseJob(id);
            List<QuartzTaskMember> noticeMembers = getQuartzTaskMembersByTaskId(quartzTask.getId());
            noticeService.sendNotice(quartzTask, noticeMembers, "禁用");
            LOGGER.info("disable job: {}", quartzTask);
        }
    }

    @Override
    public void disableByOrganizationId(long orgId) {
        QuartzTask query = new QuartzTask();
        query.setLevel(ResourceLevel.ORGANIZATION.value());
        query.setSourceId(orgId);
        List<QuartzTask> quartzTasks = taskMapper.select(query);
        quartzTasks.forEach(quartzTask -> {
            disableTaskAndPauseJob(quartzTask.getId(), quartzTask.getObjectVersionNumber(), quartzTask);
        });
    }

    @Transactional
    @Override
    public void delete(long id, String level, Long sourceId) {
        QuartzTask quartzTask = getQuartzTask(id, level, sourceId);
        if (taskMapper.deleteByPrimaryKey(id) != 1) {
            throw new CommonException("error.scheduleTask.deleteTaskFailed");
        }
        quartzJobService.removeJob(id);
        LOGGER.info("delete job: {}", quartzTask);
    }

    @Override
    public ResponseEntity<Page<QuartzTaskDTO>> pageQuery(PageRequest pageRequest, String status, String name, String description, String params, String level, Long sourceId) {
        Page<QuartzTask> page = PageHelper.doPageAndSort(pageRequest,
                () -> taskMapper.fulltextSearch(status, name, description, params, level, sourceId));
        Page<QuartzTaskDTO> pageBack = pageConvert(page);
        return new ResponseEntity<>(pageBack, HttpStatus.OK);
    }

    public Page<QuartzTaskDTO> pageConvert(Page<QuartzTask> page) {
        List<QuartzTaskDTO> quartzTaskDTOS = new ArrayList<>();
        Page<QuartzTaskDTO> pageBack = new Page<>();
        pageBack.setNumber(page.getNumber());
        pageBack.setNumberOfElements(page.getNumberOfElements());
        pageBack.setSize(page.getSize());
        pageBack.setTotalElements(page.getTotalElements());
        pageBack.setTotalPages(page.getTotalPages());
        if (page.getContent().isEmpty()) {
            return pageBack;
        } else {
            page.getContent().forEach(t -> {
                Date lastStartTime = null;
                Date nextStartTime;
                QuartzTaskInstance lastInstance = instanceMapper.selectLastInstance(t.getId());
                if (lastInstance != null) {
                    lastStartTime = lastInstance.getActualStartTime();
                    nextStartTime = lastInstance.getPlannedNextTime();
                } else {
                    // 初次执行 开始时间已过，TriggerType 为 Cron ，则设当前时间的最近执行时间为下次执行时间 ；否则 设 开始时间 为 下次执行时间
                    if (t.getStartTime().getTime() < new Date().getTime() && TriggerType.CRON.getValue().equals(t.getTriggerType())) {
                        nextStartTime = TriggerUtils.getStartTime(t.getCronExpression());
                    } else {
                        nextStartTime = t.getStartTime();
                    }
                }
                if (!QuartzDefinition.TaskStatus.ENABLE.name().equals(t.getStatus())) {
                    nextStartTime = null;
                }
                quartzTaskDTOS.add(new QuartzTaskDTO(t.getId(), t.getName(), t.getDescription(), lastStartTime, nextStartTime, t.getStatus(), t.getObjectVersionNumber()));
            });
            pageBack.setContent(quartzTaskDTOS);
            return pageBack;
        }
    }

    @Override
    public void finish(long id) {
        QuartzTask quartzTask = taskMapper.selectByPrimaryKey(id);
        if (quartzTask == null) {
            LOGGER.warn("finish job error, quartzTask is not exist {}", id);
        } else {
            quartzTask.setStatus(QuartzDefinition.TaskStatus.FINISHED.name());
            if (taskMapper.updateByPrimaryKey(quartzTask) == 1) {
                LOGGER.info("finish job: {}", quartzTask);
            } else {
                LOGGER.error("finish job error, updateStatus failed : {}", quartzTask);
            }
            List<QuartzTaskMember> noticeMembers = getQuartzTaskMembersByTaskId(quartzTask.getId());
            noticeService.sendNotice(quartzTask, noticeMembers, "结束");
        }
    }

    @Override
    public ScheduleTaskDetailDTO getTaskDetail(Long id, String level, Long sourceId) {
        QuartzTask quartzTask = getQuartzTask(id, level, sourceId);
        Date lastStartTime = null;
        Date nextStartTime;
        QuartzTaskInstance lastInstance = instanceMapper.selectLastInstance(id);
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
        List<QuartzTaskMember> members = getQuartzTaskMembersByTaskId(quartzTask.getId());
        ScheduleTaskDetailDTO detailDTO = new ScheduleTaskDetailDTO(quartzTaskDetail, objectMapper, lastStartTime, nextStartTime);
        detailDTO.setNotifyUser(getNotifyUser(members));
        //设置方法描述
        QuartzMethod query = new QuartzMethod();
        query.setCode(detailDTO.getMethodCode());
        QuartzMethod quartzMethod = methodMapper.selectOne(query);
        detailDTO.setMethodDescription(Optional.ofNullable(quartzMethod).map(QuartzMethod::getDescription).orElse(null));
        return detailDTO;
    }

    /**
     * 得到通知对象NotifyUser
     *
     * @param members
     * @return
     */
    private ScheduleTaskDetailDTO.NotifyUser getNotifyUser(List<QuartzTaskMember> members) {
        if (members == null || members.isEmpty()) return null;
        ScheduleTaskDetailDTO.User creator = null;
        Boolean administrator = false;
        List<Long> idList = new LinkedList<>();
        List<ScheduleTaskDetailDTO.User> assigners = new LinkedList<>();
        for (int i = 0; i < members.size(); i++) {
            QuartzTaskMember member = members.get(i);
            if (MemberType.ASSIGNER.value().equals(member.getMemberType())) {
                idList.add(member.getMemberId());
            } else if (MemberType.CREATOR.value().equals(member.getMemberType())) {
                Long[] creatorId = new Long[1];
                creatorId[0] = member.getMemberId();
                List<UserDTO> users = iamFeignClient.listUsersByIds(creatorId).getBody();
                if (users.size() >= 1) {
                    UserDTO userDTO = users.get(0);
                    creator = new ScheduleTaskDetailDTO.User(userDTO.getLoginName(), userDTO.getRealName());
                }
            } else if (MemberType.ROLE.value().equals(member.getMemberType())) {
                administrator = true;
            }
        }
        if (!idList.isEmpty()) {
            List<UserDTO> users = iamFeignClient.listUsersByIds(idList.toArray(new Long[0])).getBody();
            for (int i = 0; i < users.size(); i++) {
                UserDTO userDTO = users.get(i);
                ScheduleTaskDetailDTO.User user = new ScheduleTaskDetailDTO.User(userDTO.getLoginName(), userDTO.getRealName());
                assigners.add(user);
            }
        }
        return new ScheduleTaskDetailDTO.NotifyUser(creator, administrator, assigners);
    }

    @Override
    public void checkName(String name, String level, Long sourceId) {
        List<Long> ids = taskMapper.selectTaskIdByName(name, level, sourceId);
        if (!ids.isEmpty()) {
            throw new CommonException("error.scheduleTask.name.exist");
        }
    }

    @Override
    public void createTaskList(String service, List<PropertyTimedTask> scanTasks, String version) {
        //此处借用闲置属性cronexpression设置是否为一次执行：是返回"1",多次执行返回"0"
        List<QuartzTask> collect = scanTasks.stream().map(t -> ConvertUtils.convertQuartzTask(objectMapper, t, service)).collect(Collectors.toList());
        collect.forEach(i -> {
            QuartzMethod method = getQuartzMethod(i);
            QuartzTask query = new QuartzTask();
            query.setExecuteMethod(i.getExecuteMethod());
            List<QuartzTask> dbTasks = taskMapper.select(query);
            //若数据库中无相同任务名  或  数据库中有相同任务名的任务 且 每次部署执行，则 创建task（name=name+version），创建job
            Boolean byTaskName = findByTaskName(dbTasks, i.getName());
            if (!byTaskName || (byTaskName && i.getCronExpression().equals("0"))) {
                i.setName(i.getName() + ":" + version);
                //若为 同一版本下的相同任务名的 任务，不创建，不执行
                List<QuartzTask> ifEqual = dbTasks.stream().filter(t -> t.getName().equals(i.getName())).collect(Collectors.toList());
                if (ifEqual.isEmpty()) {
                    try {
                        validExecuteParams(objectMapper.readValue(i.getExecuteParams(), new TypeReference<Map<String, Object>>() {
                        }), objectMapper.readValue(method.getParams(), new TypeReference<List<PropertyJobParam>>() {
                        }));
                        if (taskMapper.insertSelective(i) != 1) {
                            throw new CommonException("error.scheduleTask.create");
                        }
                        QuartzTask db = taskMapper.selectByPrimaryKey(i.getId());
                        quartzJobService.addJob(db);
                        LOGGER.info("create job: {}", i);
                    } catch (IOException e) {
                        throw new CommonException("error.scheduleTask.createJsonIOException", e);
                    }
                }
            }
        });
    }

    private QuartzMethod getQuartzMethod(QuartzTask i) {
        QuartzMethod method = new QuartzMethod();
        method.setCode(i.getExecuteMethod());
        List<QuartzMethod> select = methodMapper.select(method);
        if (select.isEmpty()) {
            throw new CommonException("error.scheduleTask.methodNotExist");
        } else if (select.size() == 1) {
            method = select.get(0);
        }
        return method;
    }


    private Boolean findByTaskName(final List<QuartzTask> tasks, final String taskName) {
        for (QuartzTask task : tasks) {
            if (task.getName().indexOf(":") != -1) {
                String[] strings = StringUtils.delimitedListToStringArray(task.getName(), ":");
                if (strings[0].equals(taskName)) {
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    public Page<SystemNotificationDTO> pagingAllNotification(PageRequest pageRequest, String status, String content, String params, ResourceLevel level, long sourceId) {
        return PageHelper.doPageAndSort(pageRequest,
                () -> taskMapper.selectNotification(ResourceLevel.ORGANIZATION.equals(level) ? ResourceLevel.ORGANIZATION.value() : ResourceLevel.SITE.value(),
                        sourceId, ResourceLevel.ORGANIZATION.equals(level) ? ORG_NOTIFICATION_CODE : SITE_NOTIFICATION_CODE,
                        status, content, params));
    }
}
