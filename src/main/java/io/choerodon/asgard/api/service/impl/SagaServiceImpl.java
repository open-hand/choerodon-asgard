package io.choerodon.asgard.api.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.choerodon.asgard.api.dto.JsonMergeDTO;
import io.choerodon.asgard.api.dto.SagaDTO;
import io.choerodon.asgard.api.dto.SagaTaskDTO;
import io.choerodon.asgard.api.dto.SagaWithTaskDTO;
import io.choerodon.asgard.api.service.SagaService;
import io.choerodon.asgard.domain.Saga;
import io.choerodon.asgard.domain.SagaTask;
import io.choerodon.asgard.infra.mapper.SagaMapper;
import io.choerodon.asgard.infra.mapper.SagaTaskMapper;
import io.choerodon.asgard.infra.utils.ConvertUtils;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.exception.FeignException;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Service
public class SagaServiceImpl implements SagaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SagaService.class);

    private SagaMapper sagaMapper;

    private SagaTaskMapper sagaTaskMapper;

    private final ModelMapper modelMapper = new ModelMapper();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public SagaServiceImpl(SagaMapper sagaMapper, SagaTaskMapper sagaTaskMapper) {
        this.sagaMapper = sagaMapper;
        this.sagaTaskMapper = sagaTaskMapper;
    }

    @Override
    public void create(Saga saga) {
        if (StringUtils.isEmpty(saga.getCode()) || StringUtils.isEmpty(saga.getService())) {
            LOGGER.warn("error.createSaga.valid, saga : {}", saga);
            return;
        }
        Saga selectSaga = sagaMapper.selectOne(new Saga(saga.getCode()));
        if (selectSaga == null) {
            if (sagaMapper.insertSelective(saga) != 1) {
                LOGGER.warn("error.createSaga.insert, saga : {}", saga);
            }
        } else if (saga.getService().equals(selectSaga.getService())) {
            saga.setId(selectSaga.getId());
            saga.setObjectVersionNumber(selectSaga.getObjectVersionNumber());
            sagaMapper.updateByPrimaryKeySelective(saga);
        }
    }

    @Override
    public ResponseEntity<Page<SagaDTO>> pagingQuery(PageRequest pageRequest, String code, String description, String service, String params) {
        return new ResponseEntity<>(PageHelper.doPageAndSort(pageRequest,
                () -> sagaMapper.fulltextSearch(code, description, service, params)), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<SagaWithTaskDTO> query(Long id) {
        Saga saga = sagaMapper.selectByPrimaryKey(id);
        if (saga == null) {
            throw new CommonException("error.saga.notExist");
        }
        SagaWithTaskDTO dto = new SagaWithTaskDTO(saga.getId(), saga.getCode(), saga.getDescription(), saga.getInputSchema(), saga.getService());
        SagaTask query = new SagaTask();
        query.setSagaCode(saga.getCode());
        query.setIsEnabled(true);
        List<List<SagaTaskDTO>> list = new ArrayList<>(
                sagaTaskMapper.select(query).stream()
                        .map(t -> modelMapper.map(t, SagaTaskDTO.class))
                        .collect(groupingBy(SagaTaskDTO::getSeq)).values());
        try {
            for (int i = 0; i < list.size(); i++) {
                if (i == 0) {
                    list.get(0).forEach(t -> t.setInputSchema(saga.getInputSchema()));
                } else {
                    List<JsonMergeDTO> mergeDTOS = list.get(i - 1).stream()
                            .filter(t -> !StringUtils.isEmpty(t.getCode()) && !StringUtils.isEmpty(t.getOutputSchema()))
                            .map(t -> new JsonMergeDTO(t.getCode(), t.getOutputSchema())).collect(Collectors.toList());
                    String inputSchema = ConvertUtils.jsonMerge(mergeDTOS, objectMapper);
                    list.get(i).forEach(t -> t.setInputSchema(inputSchema));
                }
            }
        } catch (IOException e) {
            LOGGER.warn("error.SagaService.query.IOException {}", e.getCause());
        }
        dto.setTasks(list);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @Override
    public void delete(Long id) {
        Saga saga = sagaMapper.selectByPrimaryKey(id);
        if (saga == null) {
            throw new FeignException("error.saga.notExist");
        }
        SagaTask sagaTask = new SagaTask();
        sagaTask.setIsEnabled(true);
        sagaTask.setSagaCode(saga.getCode());
        List<SagaTask> sagaTasks = sagaTaskMapper.select(sagaTask);
        if (!sagaTasks.isEmpty()) {
            throw new FeignException("error.saga.deleteWhenTaskExist");
        }
        sagaMapper.deleteByPrimaryKey(id);
    }
}
