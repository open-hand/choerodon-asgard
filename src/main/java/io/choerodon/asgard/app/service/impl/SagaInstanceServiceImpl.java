package io.choerodon.asgard.app.service.impl;

import static java.util.stream.Collectors.groupingBy;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import io.choerodon.asgard.api.vo.*;
import io.choerodon.asgard.app.eventhandler.SagaInstanceEventPublisher;
import io.choerodon.asgard.app.service.JsonDataService;
import io.choerodon.asgard.app.service.SagaInstanceService;
import io.choerodon.asgard.app.service.SagaService;
import io.choerodon.asgard.infra.dto.*;
import io.choerodon.asgard.infra.mapper.*;
import io.choerodon.asgard.infra.utils.CommonUtils;
import io.choerodon.asgard.infra.utils.ParamUtils;
import io.choerodon.asgard.saga.SagaDefinition;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.exception.FeignException;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

@Service
public class SagaInstanceServiceImpl implements SagaInstanceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SagaInstanceService.class);

    static final String DB_ERROR = "error.db.insertOrUpdate";

    static final String ERROR_CODE_SAGA_INSTANCE_NOT_EXIST = "error.sagaInstance.notExist";

    private static final String SAGA_CODE = "sagaCode";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ModelMapper modelMapper = new ModelMapper();

    private SagaTaskMapper taskMapper;
    private SagaInstanceMapper instanceMapper;
    private SagaTaskInstanceMapper taskInstanceMapper;
    private JsonDataMapper jsonDataMapper;
    private JsonDataService jsonDataService;
    private SagaInstanceEventPublisher sagaInstanceEventPublisher;

    @Autowired
    private SagaService sagaService;
    @Autowired
    private SagaMapper sagaMapper;

    public SagaInstanceServiceImpl(SagaTaskMapper taskMapper,
                                   SagaInstanceMapper instanceMapper,
                                   SagaTaskInstanceMapper taskInstanceMapper,
                                   JsonDataService jsonDataService,
                                   JsonDataMapper jsonDataMapper,
                                   SagaInstanceEventPublisher sagaInstanceEventPublisher) {
        this.taskMapper = taskMapper;
        this.instanceMapper = instanceMapper;
        this.taskInstanceMapper = taskInstanceMapper;
        this.jsonDataService = jsonDataService;
        this.jsonDataMapper = jsonDataMapper;
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
    public ResponseEntity<Page<SagaInstanceDetails>> pageQuery(PageRequest pageable, String sagaCode, String status, String refType, String refId, String params, String level, Long sourceId, Long id) {
        Page<SagaInstanceDetails> sagaInstanceDetailsPage = PageHelper.doPageAndSort(pageable,
                () -> instanceMapper.fulltextSearchInstance(sagaCode, status, refType, refId, params, level, sourceId, id));
        sagaInstanceDetailsPage.getContent().forEach(i -> {
            i.setViewId(ParamUtils.handId(i.getId()));
            i.setSearchId(String.valueOf(i.getId()));
        });
        return new ResponseEntity<>(sagaInstanceDetailsPage, HttpStatus.OK);
    }

    @Override
    public SagaWithTaskInstance query(Long id) {
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
        return dto;
    }

    @Override
    public Map<String, Integer> statistics(String level, Long sourceId) {
        return instanceMapper.statisticsByStatus(level, sourceId);
    }

    @Override
    public List<SagaInstanceFailureVO> statisticsFailure(String level, Long sourceId, Integer date) {
        String endTime = getTimeStr(null, 1);
        String startTime = getTimeStr(null, date * (-1));

        List<SagaInstanceFailureVO> list;
        if (level != null && !level.equals("site")) {
            list = instanceMapper.statisticsFailure(level, sourceId, startTime, endTime);
        } else {
            list = instanceMapper.statisticsFailure(level, null, startTime, endTime);
        }
        if (CollectionUtils.isEmpty(list)) {
            SagaInstanceFailureVO failureVO = new SagaInstanceFailureVO(getTime(null, date * (-1)), 0L, 0.0, 0L);
            list = new ArrayList<>();
            list.add(failureVO);
        } else if (!getTimeStr(list.get(0).getCreationDate(), 0).equals(getTimeStr(null, date * (-1)))) {
            SagaInstanceFailureVO failureVO = new SagaInstanceFailureVO(getTime(null, date * (-1)), 0L, 0.0, 0L);
            list.add(0, failureVO);
        }

        if (!getTimeStr(list.get(list.size() - 1).getCreationDate(), 0).equals(getTimeStr(null, 0))) {
            SagaInstanceFailureVO failureVO = new SagaInstanceFailureVO(getTime(null, 0), 0L, 0.0, 0L);
            list.add(failureVO);
        }
        return list;
    }

    @Override
    public Page<SagaInstanceDTO> statisticsFailureList(String level, Long sourceId, Integer date, PageRequest pageable) {
        String endTime = getTimeStr(null, 1);
        String startTime = getTimeStr(null, date * (-1));
        Page<SagaInstanceDTO> pageInfo = new Page<>();
        if (level != null && !level.equals("site")) {
            pageInfo = PageHelper.doPageAndSort(pageable, () -> instanceMapper.statisticsFailureList(level, sourceId, startTime, endTime));

        } else {
            pageInfo = PageHelper
                    .doPageAndSort(pageable,
                            () -> instanceMapper.statisticsFailureList(level, null, startTime, endTime));
        }

        pageInfo.getContent().forEach(i -> {
            i.setViewId(ParamUtils.handId(i.getId()));
        });

        return pageInfo;
    }

    @Override
    public SagaInstanceDetails queryDetails(Long id) {
        return instanceMapper.selectDetails(id);
    }


    @Override
    public List<SagaInstanceDetails> queryByRefTypeAndRefIds(String refType, List<String> refIds, String sagaCode) {
        if (StringUtils.isEmpty(refType) || CollectionUtils.isEmpty(refIds)) {
            return Collections.EMPTY_LIST;
        }
        //如果业务一样，取最新的
        List<SagaInstanceDetails> instanceDetails = instanceMapper.queryByRefTypeAndRefIds(refType, refIds, sagaCode);
        if (CollectionUtils.isEmpty(instanceDetails)) {
            return Collections.EMPTY_LIST;
        }
        //填充saga的定义步骤流程
        SagaDTO sagaDTO = new SagaDTO();
        sagaDTO.setCode(sagaCode);
        SagaDTO dto = sagaMapper.selectOne(sagaDTO);
        if (Objects.isNull(dto)) {
            return Collections.EMPTY_LIST;
        }
        SagaWithTask sagaWithTask = sagaService.query(dto.getId()).getBody();

        //铺平定义
        List<String> sagaTaskList = getSagaTaskList(sagaWithTask).stream().map(SagaTask::getCode).collect(Collectors.toList());
        instanceDetails.forEach(sagaInstanceDetails -> {
            SagaTaskInstanceDTO sagaTaskInstanceDTO = new SagaTaskInstanceDTO();
            sagaTaskInstanceDTO.setSagaInstanceId(sagaInstanceDetails.getId());
            List<SagaTaskInstanceDTO> sagaTaskInstanceDTOS = taskInstanceMapper.select(sagaTaskInstanceDTO);
            //这里需要剔除定义里面查不到的code
            List<SagaTaskInstanceDTO> taskInstanceDTOS = sagaTaskInstanceDTOS.stream().filter(sagaTaskInstanceDTO1 -> sagaTaskList.contains(sagaTaskInstanceDTO1.getTaskCode())).collect(Collectors.toList());
            sagaInstanceDetails.setSagaTaskInstanceDTOS(taskInstanceDTOS);
            sagaInstanceDetails.setAllTask(sagaTaskList.size());
        });
        List<SagaInstanceDetails> sagaInstanceDetails = new ArrayList<>();
        Map<String, List<SagaInstanceDetails>> listMap = instanceDetails.stream().collect(groupingBy(SagaInstanceDetails::getRefId));
        for (Map.Entry<String, List<SagaInstanceDetails>> stringListEntry : listMap.entrySet()) {
            if (stringListEntry.getValue().size() > 1) {
                sagaInstanceDetails.add(stringListEntry.getValue().stream().sorted(Comparator.comparing(SagaInstanceDetails::getId).reversed()).collect(Collectors.toList()).get(0));
            } else {
                sagaInstanceDetails.add(stringListEntry.getValue().get(0));
            }
        }
        return sagaInstanceDetails;
    }

    private Integer getAllTask(SagaWithTask sagaWithTask) {
        if (Objects.isNull(sagaWithTask) || CollectionUtils.isEmpty(sagaWithTask.getTasks())) {
            return 0;
        }
        return sagaWithTask.getTasks().stream().map(List::size).reduce((integer, integer2) -> integer + integer2).orElseGet(() -> 0);
    }

    private List<SagaTask> getSagaTaskList(SagaWithTask sagaWithTask) {
        List<SagaTask> sagaTasks = new ArrayList<>();
        if (Objects.isNull(sagaWithTask) || CollectionUtils.isEmpty(sagaWithTask.getTasks())) {
            return Collections.EMPTY_LIST;
        }

        sagaWithTask.getTasks().forEach(tasks -> {
            sagaTasks.addAll(tasks);
        });
        return sagaTasks;
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
            LOGGER.warn("sagaInstance is notExist,uuid:{}", uuid);
            return;
        }
        List<PageSagaTaskInstance> taskInstanceList = taskInstanceMapper.selectAllBySagaInstanceId(dbInstance.getId());
        if (CollectionUtils.isEmpty(taskInstanceList)) {
            instanceMapper.deleteByPrimaryKey(dbInstance.getId());
        } else {
            throw new CommonException("error.cancel.saga.instance");
        }
    }

    private Date getTime(Date date, Integer num) {
        date = date == null ? new Date() : date;
        Calendar calStart = Calendar.getInstance();
        calStart.setTime(date);
        calStart.add(Calendar.DAY_OF_MONTH, num);
        return calStart.getTime();
    }

    private String getTimeStr(Date date, Integer num) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(getTime(date, num));
    }
}
