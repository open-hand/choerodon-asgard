package io.choerodon.asgard.api.service.impl;

import static java.util.stream.Collectors.groupingBy;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.asgard.api.dto.*;
import io.choerodon.asgard.api.service.SagaInstanceService;
import io.choerodon.asgard.domain.JsonData;
import io.choerodon.asgard.domain.SagaInstance;
import io.choerodon.asgard.domain.SagaTask;
import io.choerodon.asgard.domain.SagaTaskInstance;
import io.choerodon.asgard.infra.mapper.*;
import io.choerodon.asgard.saga.SagaDefinition;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.exception.FeignException;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

@Service
public class SagaInstanceServiceImpl implements SagaInstanceService {

    public static final String DB_ERROR = "error.db.insertOrUpdate";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ModelMapper modelMapper = new ModelMapper();

    private SagaMapper sagaMapper;
    private SagaTaskMapper taskMapper;
    private SagaInstanceMapper instanceMapper;
    private SagaTaskInstanceMapper taskInstanceMapper;
    private JsonDataMapper jsonDataMapper;


    public SagaInstanceServiceImpl(SagaMapper sagaMapper,
                                   SagaTaskMapper taskMapper,
                                   SagaInstanceMapper instanceMapper,
                                   SagaTaskInstanceMapper taskInstanceMapper,
                                   JsonDataMapper jsonDataMapper) {
        this.sagaMapper = sagaMapper;
        this.taskMapper = taskMapper;
        this.instanceMapper = instanceMapper;
        this.taskInstanceMapper = taskInstanceMapper;
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
        final String code = dto.getSagaCode();
        if (!sagaMapper.existByCode(code)) {
            throw new FeignException("error.saga.notExist");
        }
        SagaTask sagaTask = new SagaTask();
        sagaTask.setIsEnabled(true);
        sagaTask.setSagaCode(code);
        List<SagaTask> sagaTasks = taskMapper.select(sagaTask);
        if (sagaTasks.isEmpty()) {
            Date date = new Date();
            SagaInstance instanceDO = new SagaInstance(code, dto.getRefType(), dto.getRefId(),
                    SagaDefinition.InstanceStatus.NON_CONSUMER.name(), date, date, dto.getLevel(), dto.getSourceId());
            instanceMapper.insertSelective(instanceDO);
            return new ResponseEntity<>(modelMapper.map(
                    instanceMapper.selectByPrimaryKey(instanceDO.getId()), SagaInstanceDTO.class), HttpStatus.OK);
        }

        return new ResponseEntity<>(startInstanceAndTask(dto, sagaTasks), HttpStatus.OK);
    }

    private SagaInstanceDTO startInstanceAndTask(final StartInstanceDTO dto, final List<SagaTask> sagaTasks) {
        final Date startTime = new Date(System.currentTimeMillis());
        SagaInstance instance = new SagaInstance(dto.getSagaCode(), dto.getRefType(), dto.getRefId(),
                SagaDefinition.TaskInstanceStatus.RUNNING.name(), startTime, dto.getLevel(), dto.getSourceId());
        Long inputDataId = null;
        if (dto.getInput() != null) {
            JsonData jsonData = new JsonData(dto.getInput());
            jsonDataMapper.insertSelective(jsonData);
            inputDataId = jsonData.getId();
            instance.setInputDataId(inputDataId);
        }
        instanceMapper.insertSelective(instance);
        Map<Integer, List<SagaTask>> taskMap = sagaTasks.stream().collect(groupingBy(SagaTask::getSeq));
        int i = 0;
        for (Map.Entry<Integer, List<SagaTask>> entry : taskMap.entrySet()) {
            if (i < 1) {
                i++;
                addRunningTask(entry.getValue(), dto, instance.getId(), inputDataId, startTime, true);
            } else {
                addRunningTask(entry.getValue(), dto, instance.getId(), inputDataId, startTime, false);
            }
        }
        return modelMapper.map(instanceMapper.selectByPrimaryKey(instance.getId()), SagaInstanceDTO.class);
    }

    private void addRunningTask(final List<SagaTask> sagaTaskList, final StartInstanceDTO dto,
                                final Long instanceId, final Long inputDataId,
                                final Date startTime, boolean running) {
        sagaTaskList.forEach(t -> {
            SagaTaskInstance sagaTaskInstance = modelMapper.map(t, SagaTaskInstance.class);
            sagaTaskInstance.setSagaInstanceId(instanceId);
            sagaTaskInstance.setPlannedStartTime(startTime);
            sagaTaskInstance.setStatus(SagaDefinition.TaskInstanceStatus.RUNNING.name());
            sagaTaskInstance.setRefId(dto.getRefId());
            sagaTaskInstance.setRefType(dto.getRefType());
            if (running) {
                sagaTaskInstance.setInputDataId(inputDataId);
                sagaTaskInstance.setStatus(SagaDefinition.TaskInstanceStatus.RUNNING.name());
            } else {
                sagaTaskInstance.setStatus(SagaDefinition.TaskInstanceStatus.QUEUE.name());
            }
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
            throw new CommonException("error.sagaInstance.notExist");
        }
        SagaWithTaskInstanceDTO dto = modelMapper.map(sagaInstance, SagaWithTaskInstanceDTO.class);
        if (sagaInstance.getInputDataId() != null) {
            dto.setInput(jsonDataMapper.selectByPrimaryKey(sagaInstance.getInputDataId()).getData());
        }
        if (sagaInstance.getOutputDataId() != null) {
            dto.setOutput(jsonDataMapper.selectByPrimaryKey(sagaInstance.getOutputDataId()).getData());
        }
        List<List<SagaTaskInstanceDTO>> list = new ArrayList<>(
                taskInstanceMapper.selectAllBySagaInstanceId(id)
                        .stream()
                        .collect(groupingBy(SagaTaskInstanceDTO::getSeq)).values());
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
}
