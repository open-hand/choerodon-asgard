package io.choerodon.asgard.api.service.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.choerodon.asgard.api.dto.*;
import io.choerodon.asgard.api.service.JsonDataService;
import io.choerodon.asgard.api.service.SagaInstanceService;
import io.choerodon.asgard.domain.SagaInstance;
import io.choerodon.asgard.domain.SagaTask;
import io.choerodon.asgard.domain.SagaTaskInstance;
import io.choerodon.asgard.infra.mapper.JsonDataMapper;
import io.choerodon.asgard.infra.mapper.SagaInstanceMapper;
import io.choerodon.asgard.infra.mapper.SagaTaskInstanceMapper;
import io.choerodon.asgard.infra.mapper.SagaTaskMapper;
import io.choerodon.asgard.infra.utils.CommonUtils;
import io.choerodon.asgard.saga.SagaDefinition;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.exception.FeignException;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

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


    public SagaInstanceServiceImpl(SagaTaskMapper taskMapper,
                                   SagaInstanceMapper instanceMapper,
                                   SagaTaskInstanceMapper taskInstanceMapper,
                                   JsonDataService jsonDataService,
                                   JsonDataMapper jsonDataMapper) {
        this.taskMapper = taskMapper;
        this.instanceMapper = instanceMapper;
        this.taskInstanceMapper = taskInstanceMapper;
        this.jsonDataService = jsonDataService;
        this.jsonDataMapper = jsonDataMapper;
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"));
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        modelMapper.addMappings(new PropertyMap<SagaTask, SagaTaskInstance>() {
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
    public ResponseEntity<SagaInstanceDTO> start(final StartInstanceDTO dto) {
        List<SagaTask> firstSeqSagaTasks = taskMapper.selectFirstSeqSagaTasks(dto.getSagaCode());
        if (firstSeqSagaTasks.isEmpty()) {
            Date date = new Date();
            SagaInstance instanceDO = new SagaInstance(dto.getSagaCode(), dto.getRefType(), dto.getRefId(),
                    SagaDefinition.InstanceStatus.NON_CONSUMER.name(), date, date, dto.getLevel(), dto.getSourceId());
            if (instanceMapper.insertSelective(instanceDO) != 1) {
                throw new FeignException(DB_ERROR);
            }
            return new ResponseEntity<>(modelMapper.map(instanceDO, SagaInstanceDTO.class), HttpStatus.OK);
        }
        return new ResponseEntity<>(startInstanceAndTask(dto, firstSeqSagaTasks), HttpStatus.OK);
    }

    private SagaInstanceDTO startInstanceAndTask(final StartInstanceDTO dto, final List<SagaTask> firstSeqSagaTasks) {
        final Date startTime = new Date(System.currentTimeMillis());
        SagaInstance instance = new SagaInstance(dto.getSagaCode(), SagaDefinition.InstanceStatus.RUNNING.name(),
                startTime, dto.getLevel(), dto.getSourceId());
        instance.setRefType(dto.getRefType());
        instance.setRefId(dto.getRefId());
        instance.setUserDetails(CommonUtils.getUserDetailsJson(objectMapper));
        instance.setInputDataId(jsonDataService.insertAndGetId(dto.getInput()));
        if (instanceMapper.insertSelective(instance) != 1) {
            throw new FeignException(DB_ERROR);
        }
        addRunningTask(firstSeqSagaTasks, instance, startTime);
        return modelMapper.map(instance, SagaInstanceDTO.class);
    }

    private void addRunningTask(final List<SagaTask> sagaTaskList, final SagaInstance instance, final Date startTime) {
        sagaTaskList.forEach(t -> {
            SagaTaskInstance sagaTaskInstance = modelMapper.map(t, SagaTaskInstance.class);
            sagaTaskInstance.setSagaInstanceId(instance.getId());
            sagaTaskInstance.setPlannedStartTime(startTime);
            sagaTaskInstance.setInputDataId(instance.getInputDataId());
            sagaTaskInstance.setStatus(SagaDefinition.TaskInstanceStatus.WAIT_TO_BE_PULLED.name());
            if (taskInstanceMapper.insertSelective(sagaTaskInstance) != 1) {
                throw new FeignException(DB_ERROR);
            }
        });
    }

    @Override
    public ResponseEntity<Page<SagaInstanceDTO>> pageQuery(PageRequest pageRequest, String sagaCode,
                                                           String status, String refType,
                                                           String refId, String params, String level, Long sourceId) {

        return new ResponseEntity<>(PageHelper.doPageAndSort(pageRequest,
                () -> instanceMapper.fulltextSearchInstance(sagaCode, status, refType, refId, params, level, sourceId)), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> query(Long id) {
        SagaInstance sagaInstance = instanceMapper.selectByPrimaryKey(id);
        if (sagaInstance == null) {
            throw new CommonException(ERROR_CODE_SAGA_INSTANCE_NOT_EXIST);
        }
        SagaWithTaskInstanceDTO dto = modelMapper.map(sagaInstance, SagaWithTaskInstanceDTO.class);
        if (sagaInstance.getInputDataId() != null) {
            dto.setInput(jsonDataMapper.selectByPrimaryKey(sagaInstance.getInputDataId()).getData());
        }
        if (sagaInstance.getOutputDataId() != null) {
            dto.setOutput(jsonDataMapper.selectByPrimaryKey(sagaInstance.getOutputDataId()).getData());
        }
        List<List<PageSagaTaskInstanceDTO>> list = new ArrayList<>(
                taskInstanceMapper.selectAllBySagaInstanceId(id)
                        .stream()
                        .collect(groupingBy(PageSagaTaskInstanceDTO::getSeq)).values().stream().sorted((List<PageSagaTaskInstanceDTO> list1, List<PageSagaTaskInstanceDTO> list2) -> {
                    PageSagaTaskInstanceDTO o1 = list1.get(0);
                    PageSagaTaskInstanceDTO o2 = list2.get(0);
                    return o1.getSeq().compareTo(o2.getSeq());
                }).collect(Collectors.toList()));
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
    public SagaInstanceDetailsDTO queryDetails(Long id) {
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
            LOGGER.info("error.sagaInstanceService.queryFailedByDate.ParseException", e);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("date", date);
        map.put("data", data);
        return map;
    }

    @Override
    public ResponseEntity<SagaInstanceDTO> preCreate(StartInstanceDTO dto) {
        SagaInstance instance = new SagaInstance(dto.getSagaCode(), SagaDefinition.InstanceStatus.UN_CONFIRMED.name(),
                new Date(), dto.getLevel(), dto.getSourceId());
        instance.setCreatedOn(dto.getService());
        instance.setUuid(dto.getUuid());
        instance.setUserDetails(CommonUtils.getUserDetailsJson(objectMapper));
        if (instanceMapper.insertSelective(instance) != 1) {
            throw new FeignException(DB_ERROR);
        }
        return new ResponseEntity<>(modelMapper.map(instance, SagaInstanceDTO.class), HttpStatus.OK);
    }

    @Transactional
    @Override
    public void confirm(String uuid, String payloadJson, String refType, String refId) {
        //根据uuid查询saga实例
        SagaInstance uuidQuery = new SagaInstance();
        uuidQuery.setUuid(uuid);
        SagaInstance dbInstance = instanceMapper.selectOne(uuidQuery);
        if (dbInstance == null) {
            throw new FeignException(ERROR_CODE_SAGA_INSTANCE_NOT_EXIST);
        }
        //查询seq为最小的task实例
        List<SagaTask> firstSeqSagaTasks = taskMapper.selectFirstSeqSagaTasks(dbInstance.getSagaCode());
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
        SagaInstance uuidQuery = new SagaInstance();
        uuidQuery.setUuid(uuid);
        SagaInstance dbInstance = instanceMapper.selectOne(uuidQuery);
        if (dbInstance == null) {
            throw new FeignException(ERROR_CODE_SAGA_INSTANCE_NOT_EXIST);
        }
        instanceMapper.deleteByPrimaryKey(dbInstance.getId());
    }
}
