package io.choerodon.asgard.api.service.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.choerodon.asgard.api.dto.SagaInstanceDTO;
import io.choerodon.asgard.api.dto.SagaTaskInstanceDTO;
import io.choerodon.asgard.api.dto.SagaWithTaskInstanceDTO;
import io.choerodon.asgard.api.dto.StartInstanceDTO;
import io.choerodon.asgard.api.service.SagaInstanceService;
import io.choerodon.asgard.domain.JsonData;
import io.choerodon.asgard.domain.SagaInstance;
import io.choerodon.asgard.domain.SagaTask;
import io.choerodon.asgard.domain.SagaTaskInstance;
import io.choerodon.asgard.infra.mapper.*;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.exception.FeignException;
import io.choerodon.core.saga.SagaDefinition;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

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


    public SagaInstanceServiceImpl(SagaMapper sagaMapper, SagaTaskMapper taskMapper,
                                   SagaInstanceMapper instanceMapper,
                                   SagaTaskInstanceMapper taskInstanceMapper,
                                   JsonDataMapper jsonDataMapper) {
        this.sagaMapper = sagaMapper;
        this.taskMapper = taskMapper;
        this.instanceMapper = instanceMapper;
        this.taskInstanceMapper = taskInstanceMapper;
        this.jsonDataMapper = jsonDataMapper;
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
                    SagaDefinition.InstanceStatus.NON_CONSUMER.name(), date, date);
            if (instanceMapper.insertSelective(instanceDO) != 1) {
                throw new FeignException(DB_ERROR);
            }
            return new ResponseEntity<>(modelMapper.map(
                    instanceMapper.selectByPrimaryKey(instanceDO.getId()), SagaInstanceDTO.class), HttpStatus.OK);
        }

        return new ResponseEntity<>(startInstanceAndTask(dto, sagaTasks), HttpStatus.OK);
    }

    @Transactional
    public SagaInstanceDTO startInstanceAndTask(final StartInstanceDTO dto, final List<SagaTask> sagaTasks) {
        SagaInstance instance = new SagaInstance(dto.getSagaCode(), dto.getRefType(), dto.getRefId(),
                SagaDefinition.TaskInstanceStatus.RUNNING.name(), new Date());
        Long inputDataId = null;
        if (dto.getInput() != null) {
            JsonData jsonData = new JsonData(dto.getInput());
            if (jsonDataMapper.insertSelective(jsonData) != 1) {
                throw new FeignException(DB_ERROR);
            }
            inputDataId = jsonData.getId();
            instance.setInputDataId(inputDataId);
        }
        if (instanceMapper.insertSelective(instance) != 1) {
            throw new FeignException(DB_ERROR);
        }
        Map<Integer, List<SagaTask>> taskMap = sagaTasks.stream().collect(groupingBy(SagaTask::getSeq));
        int i = 0;
        for (Map.Entry<Integer, List<SagaTask>> entry : taskMap.entrySet()) {
            if (i < 1) {
                i++;
                addRunningTask(entry.getValue(), dto, instance.getId(), inputDataId, true);
            } else {
                addRunningTask(entry.getValue(), dto, instance.getId(), inputDataId, false);
            }
        }
        return modelMapper.map(instanceMapper.selectByPrimaryKey(instance.getId()), SagaInstanceDTO.class);
    }

    private void addRunningTask(final List<SagaTask> sagaTaskList, final StartInstanceDTO dto,
                                final Long instanceId, final Long inputDataId, boolean running) {
        sagaTaskList.forEach(t -> {
            SagaTaskInstance sagaTaskInstance = modelMapper.map(t, SagaTaskInstance.class);
            sagaTaskInstance.setSagaInstanceId(instanceId);
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
                                                           String refId, String params) {

        return new ResponseEntity<>(PageHelper.doPageAndSort(pageRequest,
                () -> instanceMapper.fulltextSearch(sagaCode, status, refType, refId, params)), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<SagaWithTaskInstanceDTO> query(Long id) {
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
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }
}
