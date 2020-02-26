package io.choerodon.asgard.app.service.impl;

import static java.util.stream.Collectors.groupingBy;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.asgard.api.vo.*;
import io.choerodon.asgard.app.eventhandler.SagaInstanceEventPublisher;
import io.choerodon.asgard.app.service.JsonDataService;
import io.choerodon.asgard.app.service.SagaInstanceService;
import io.choerodon.asgard.infra.dto.JsonDataDTO;
import io.choerodon.asgard.infra.dto.SagaInstanceDTO;
import io.choerodon.asgard.infra.dto.SagaTaskDTO;
import io.choerodon.asgard.infra.dto.SagaTaskInstanceDTO;
import io.choerodon.asgard.infra.feign.IamFeignClient;
import io.choerodon.asgard.infra.mapper.JsonDataMapper;
import io.choerodon.asgard.infra.mapper.SagaInstanceMapper;
import io.choerodon.asgard.infra.mapper.SagaTaskInstanceMapper;
import io.choerodon.asgard.infra.mapper.SagaTaskMapper;
import io.choerodon.asgard.infra.utils.CommonUtils;
import io.choerodon.asgard.saga.SagaDefinition;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.exception.FeignException;
import io.choerodon.web.util.PageableHelper;

@Service
public class SagaInstanceServiceImpl implements SagaInstanceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SagaInstanceService.class);

    static final String DB_ERROR = "error.db.insertOrUpdate";

    static final String ERROR_CODE_SAGA_INSTANCE_NOT_EXIST = "error.sagaInstance.notExist";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ModelMapper modelMapper = new ModelMapper();

    private SagaTaskMapper taskMapper;
    private SagaInstanceMapper instanceMapper;
    private SagaTaskInstanceMapper taskInstanceMapper;
    private JsonDataMapper jsonDataMapper;
    private JsonDataService jsonDataService;
    private SagaInstanceEventPublisher sagaInstanceEventPublisher;
    private IamFeignClient iamFeignClient;


    public SagaInstanceServiceImpl(SagaTaskMapper taskMapper,
                                   SagaInstanceMapper instanceMapper,
                                   SagaTaskInstanceMapper taskInstanceMapper,
                                   JsonDataService jsonDataService,
                                   JsonDataMapper jsonDataMapper,
                                   IamFeignClient iamFeignClient,
                                   SagaInstanceEventPublisher sagaInstanceEventPublisher) {
        this.taskMapper = taskMapper;
        this.instanceMapper = instanceMapper;
        this.taskInstanceMapper = taskInstanceMapper;
        this.jsonDataService = jsonDataService;
        this.jsonDataMapper = jsonDataMapper;
        this.iamFeignClient = iamFeignClient;
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"));
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        this.sagaInstanceEventPublisher = sagaInstanceEventPublisher;
        modelMapper.addMappings(new PropertyMap<SagaTaskDTO, SagaTaskInstanceDTO>() {
            @Override
            protected void configure() {
                skip(destination.getId());
                skip(destination.getCreationDate());
                skip(destination.getCreatedBy());
                skip(destination.getLastUpdateDate());
                skip(destination.getLastUpdatedBy());
                skip(destination.getObjectVersionNumber());
            }
        });
    }

    @Override
    @Transactional
    public ResponseEntity<SagaInstance> start(final StartInstance dto) {
        List<SagaTaskDTO> firstSeqSagaTasks = taskMapper.selectFirstSeqSagaTasks(dto.getSagaCode());
        if (firstSeqSagaTasks.isEmpty()) {
            Date date = new Date();
            SagaInstanceDTO instanceDO = new SagaInstanceDTO(dto.getSagaCode(), dto.getRefType(), dto.getRefId(),
                    SagaDefinition.InstanceStatus.NON_CONSUMER.name(), date, date, dto.getLevel(), dto.getSourceId());
            if (instanceMapper.insertSelective(instanceDO) != 1) {
                throw new FeignException(DB_ERROR);
            }
            return new ResponseEntity<>(modelMapper.map(instanceDO, SagaInstance.class), HttpStatus.OK);
        }
        return new ResponseEntity<>(startInstanceAndTask(dto, firstSeqSagaTasks), HttpStatus.OK);
    }

    private SagaInstance startInstanceAndTask(final StartInstance dto, final List<SagaTaskDTO> firstSeqSagaTasks) {
        final Date startTime = new Date(System.currentTimeMillis());
        SagaInstanceDTO instance = new SagaInstanceDTO(dto.getSagaCode(), SagaDefinition.InstanceStatus.RUNNING.name(),
                startTime, dto.getLevel(), dto.getSourceId());
        instance.setRefType(dto.getRefType());
        instance.setRefId(dto.getRefId());
        instance.setUserDetails(CommonUtils.getUserDetailsJson(objectMapper));
        instance.setInputDataId(jsonDataService.insertAndGetId(dto.getInput()));
        if (instanceMapper.insertSelective(instance) != 1) {
            throw new FeignException(DB_ERROR);
        }
        addRunningTask(firstSeqSagaTasks, instance, startTime);
        return modelMapper.map(instance, SagaInstance.class);
    }

    private void addRunningTask(final List<SagaTaskDTO> sagaTaskList, final SagaInstanceDTO instance, final Date startTime) {
        sagaTaskList.forEach(t -> {
            SagaTaskInstanceDTO sagaTaskInstance = modelMapper.map(t, SagaTaskInstanceDTO.class);
            sagaTaskInstance.setSagaInstanceId(instance.getId());
            sagaTaskInstance.setPlannedStartTime(startTime);
            sagaTaskInstance.setInputDataId(instance.getInputDataId());
            sagaTaskInstance.setStatus(SagaDefinition.TaskInstanceStatus.WAIT_TO_BE_PULLED.name());
            if (taskInstanceMapper.insertSelective(sagaTaskInstance) != 1) {
                throw new FeignException(DB_ERROR);
            } else {
                sagaInstanceEventPublisher.sagaTaskInstanceEvent(t.getService());
            }
        });
    }

    @Override
    public ResponseEntity<PageInfo<SagaInstanceDetails>> pageQuery(Pageable pageable, String sagaCode, String status, String refType, String refId, String params, String level, Long sourceId) {
        return new ResponseEntity<>(
                PageHelper
                        .startPage(pageable.getPageNumber(), pageable.getPageSize())
                        .doSelectPageInfo(
                                () -> instanceMapper.fulltextSearchInstance(sagaCode, status, refType, refId, params, level, sourceId)), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> query(Long id) {
        SagaInstanceDTO sagaInstance = instanceMapper.selectByPrimaryKey(id);
        if (sagaInstance == null) {
            throw new CommonException(ERROR_CODE_SAGA_INSTANCE_NOT_EXIST);
        }
        SagaWithTaskInstance dto = modelMapper.map(sagaInstance, SagaWithTaskInstance.class);
        Long inputDataId = sagaInstance.getInputDataId();
        Long outputDataId = sagaInstance.getOutputDataId();
        if (inputDataId != null) {
            JsonDataDTO inputJsonData = jsonDataMapper.selectByPrimaryKey(inputDataId);
            if (inputJsonData == null) {
                LOGGER.warn("input data of saga instance is null which input data id = {}", inputDataId);
            } else {
                dto.setInput(inputJsonData.getData());
            }
        }
        if (outputDataId != null) {
            JsonDataDTO outputJsonData = jsonDataMapper.selectByPrimaryKey(outputDataId);
            if (outputJsonData == null) {
                LOGGER.warn("output data of saga instance is null which output data id = {}", outputDataId);
            } else {
                dto.setOutput(outputJsonData.getData());
            }
        }
        List<List<PageSagaTaskInstance>> list = taskInstanceMapper.selectAllBySagaInstanceId(id)
                .stream()
                .collect(groupingBy(PageSagaTaskInstance::getSeq)).values().stream().sorted((List<PageSagaTaskInstance> list1, List<PageSagaTaskInstance> list2) -> {
                    PageSagaTaskInstance o1 = list1.get(0);
                    PageSagaTaskInstance o2 = list2.get(0);
                    return o1.getSeq().compareTo(o2.getSeq());
                }).collect(Collectors.toList());
        dto.setTasks(list);
        try {
            return new ResponseEntity<>(objectMapper.writeValueAsString(dto), HttpStatus.OK);
        } catch (JsonProcessingException e) {
            throw new CommonException("error.SagaInstanceService.IOException", e);
        }
    }

    @Override
    public Map<String, Integer> statistics(String level, Long sourceId) {
        return instanceMapper.statisticsByStatus(level, sourceId);
    }

    @Override
    public List<SagaInstanceFailureVO> statisticsFailure(String level, Long sourceId, Integer date) {
        String endTime = getTimeStr(null, 1);
        String startTime = getTimeStr(null, date * (-1));

        if (level != null && !level.equals("site")) {
            List<ProjectVO> projectVOs = iamFeignClient.listProjectsByOrgId(sourceId).getBody();
            List<Long> projectIds = projectVOs == null ? null : projectVOs.stream().map(ProjectVO::getId).collect(Collectors.toList());
            return instanceMapper.statisticsFailure(level, sourceId, startTime, endTime, projectIds);
        } else {
            return instanceMapper.statisticsFailure(level, null, startTime, endTime, null);
        }
    }

    @Override
    public PageInfo<SagaInstanceDTO> statisticsFailureList(String level, Long sourceId, Integer date, Pageable pageable) {
        String endTime = getTimeStr(null, 1);
        String startTime = getTimeStr(null, date * (-1));

        if (level != null && !level.equals("site")) {
            List<ProjectVO> projectVOs = iamFeignClient.listProjectsByOrgId(sourceId).getBody();
            List<Long> projectIds = projectVOs == null ? null : projectVOs.stream().map(ProjectVO::getId).collect(Collectors.toList());
            return PageHelper
                    .startPage(pageable.getPageNumber(), pageable.getPageSize(), PageableHelper.getSortSql(pageable.getSort()))
                    .doSelectPageInfo(
                            () -> instanceMapper.statisticsFailureList(level, sourceId, startTime, endTime, projectIds));
        } else {
            return PageHelper
                    .startPage(pageable.getPageNumber(), pageable.getPageSize(), PageableHelper.getSortSql(pageable.getSort()))
                    .doSelectPageInfo(
                            () -> instanceMapper.statisticsFailureList(level, null, startTime, endTime, null));
        }
    }

    @Override
    public SagaInstanceFailureDetailVO statisticsFailureDetail(String level, Long sourceId, String dateStr) {
        SagaInstanceFailureDetailVO sagaInstanceFailureDetailVO;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date;
        try {
            date = sdf.parse(dateStr);
        } catch (ParseException e) {
            throw new CommonException("error.get.saga.instance.failure.detail");
        }

        String startTime = getTimeStr(date, -1);
        String endTime = getTimeStr(date, 1);

        if (level != null && !level.equals("site")) {
            List<ProjectVO> projectVOs = iamFeignClient.listProjectsByOrgId(sourceId).getBody();
            List<Long> projectIds = projectVOs == null ? null : projectVOs.stream().map(ProjectVO::getId).collect(Collectors.toList());
            sagaInstanceFailureDetailVO = instanceMapper.statisticsFailureDetail(level, sourceId, startTime, endTime, projectIds);
        } else {
            sagaInstanceFailureDetailVO = instanceMapper.statisticsFailureDetail(level, null, startTime, endTime, null);
        }
        sagaInstanceFailureDetailVO.setDate(dateStr);

        DecimalFormat df = new DecimalFormat("0.00");
        sagaInstanceFailureDetailVO.setPercentage(df.format((float) sagaInstanceFailureDetailVO.getFailureCount() / sagaInstanceFailureDetailVO.getTotalCount() * 100));
        return sagaInstanceFailureDetailVO;
    }

    @Override
    public SagaInstanceDetails queryDetails(Long id) {
        return instanceMapper.selectDetails(id);
    }

    @Override
    public Map<String, Object> queryFailedByDate(String beginDate, String endDate) {
        List<String> date = new ArrayList<>();
        List<Long> data = new ArrayList<>();
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            Date begin = dateFormat.parse(beginDate);
            Date end = dateFormat.parse(endDate);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(begin);
            List<Map<String, Object>> maps = instanceMapper.selectFailedTimes(dateFormat.format(begin), dateFormat.format(end));
            List<Object> days = maps.stream().map(m -> m.get("days")).collect(Collectors.toList());
            while (true) {
                if (calendar.getTime().after(end)) {
                    break;
                }
                String format = dateFormat.format(calendar.getTime());
                date.add(format);
                Long count = 0L;
                if (days.contains(format)) {
                    count = (Long) maps.get(days.indexOf(format)).get("count");
                }
                data.add(count);
                calendar.add(Calendar.DATE, 1);
            }
        } catch (ParseException e) {
            LOGGER.error("error.sagaInstanceService.queryFailedByDate.ParseException", e);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("date", date);
        map.put("data", data);
        return map;
    }

    @Override
    public ResponseEntity<SagaInstance> preCreate(StartInstance dto) {
        SagaInstanceDTO instance = new SagaInstanceDTO(dto.getSagaCode(), SagaDefinition.InstanceStatus.UN_CONFIRMED.name(),
                new Date(), dto.getLevel(), dto.getSourceId());
        instance.setCreatedOn(dto.getService());
        instance.setUuid(dto.getUuid());
        instance.setUserDetails(CommonUtils.getUserDetailsJson(objectMapper));
        if (instanceMapper.insertSelective(instance) != 1) {
            throw new FeignException(DB_ERROR);
        }
        return new ResponseEntity<>(modelMapper.map(instance, SagaInstance.class), HttpStatus.OK);
    }

    @Transactional
    @Override
    public void confirm(String uuid, String payloadJson, String refType, String refId) {
        //根据uuid查询saga实例
        SagaInstanceDTO uuidQuery = new SagaInstanceDTO();
        uuidQuery.setUuid(uuid);
        SagaInstanceDTO dbInstance = instanceMapper.selectOne(uuidQuery);
        if (dbInstance == null) {
            throw new FeignException(ERROR_CODE_SAGA_INSTANCE_NOT_EXIST);
        }
        //查询seq为最小的task实例
        List<SagaTaskDTO> firstSeqSagaTasks = taskMapper.selectFirstSeqSagaTasks(dbInstance.getSagaCode());
        final Date startTime = new Date(System.currentTimeMillis());
        dbInstance.setRefType(refType);
        dbInstance.setRefId(refId);
        //该saga没有task，则直接将状态设置为NON_CONSUMER
        if (firstSeqSagaTasks.isEmpty()) {
            dbInstance.setStatus(SagaDefinition.InstanceStatus.NON_CONSUMER.name());
            dbInstance.setEndTime(startTime);
            //该saga有task，则设置状态为RUNNING，并设置输入
        } else {
            dbInstance.setInputDataId(jsonDataService.insertAndGetId(payloadJson));
            dbInstance.setStatus(SagaDefinition.InstanceStatus.RUNNING.name());
        }
        if (instanceMapper.updateByPrimaryKey(dbInstance) != 1) {
            throw new FeignException(DB_ERROR);
        }
        //创建task实例
        addRunningTask(firstSeqSagaTasks, dbInstance, startTime);
    }

    @Override
    public void cancel(String uuid) {
        SagaInstanceDTO uuidQuery = new SagaInstanceDTO();
        uuidQuery.setUuid(uuid);
        SagaInstanceDTO dbInstance = instanceMapper.selectOne(uuidQuery);
        if (dbInstance == null) {
            throw new FeignException(ERROR_CODE_SAGA_INSTANCE_NOT_EXIST);
        }
        List<PageSagaTaskInstance> taskInstanceList = taskInstanceMapper.selectAllBySagaInstanceId(dbInstance.getId());
        if (taskInstanceList == null || taskInstanceList.size() == 0) {
            instanceMapper.deleteByPrimaryKey(dbInstance.getId());
        } else {
            throw new CommonException("error.cancel.saga.instance");
        }
    }

    private String getTimeStr(Date date, Integer num) {
        date = date == null ? new Date() : date;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calStart = Calendar.getInstance();
        calStart.setTime(date);
        calStart.add(Calendar.DAY_OF_MONTH, num);
        return dateFormat.format(calStart.getTime());
    }
}
