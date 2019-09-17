package io.choerodon.asgard.app.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.choerodon.asgard.api.vo.*;
import io.choerodon.asgard.app.service.SagaService;
import io.choerodon.asgard.infra.dto.SagaDTO;
import io.choerodon.asgard.infra.dto.SagaTaskDTO;
import io.choerodon.asgard.infra.mapper.SagaMapper;
import io.choerodon.asgard.infra.mapper.SagaTaskMapper;
import io.choerodon.asgard.infra.utils.ConvertUtils;
import io.choerodon.asgard.infra.utils.ParamUtils;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.exception.CommonException;
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

    public void setSagaMapper(SagaMapper sagaMapper) {
        this.sagaMapper = sagaMapper;
    }

    @Override
    public void create(SagaDTO saga) {
        if (StringUtils.isEmpty(saga.getCode()) || StringUtils.isEmpty(saga.getService())) {
            LOGGER.warn("error.createSaga.valid, saga : {}", saga);
            return;
        }
        SagaDTO selectSaga = sagaMapper.selectOne(new SagaDTO(saga.getCode()));
        if (selectSaga == null) {
            sagaMapper.insertSelective(saga);
        } else if (saga.getService().equals(selectSaga.getService())) {
            saga.setId(selectSaga.getId());
            saga.setObjectVersionNumber(selectSaga.getObjectVersionNumber());
            sagaMapper.updateByPrimaryKeySelective(saga);
        }
    }

    @Override
    public ResponseEntity<PageInfo<Saga>> pagingQuery(PageRequest pageRequest, String code, String description, String service, String param) {
        return new ResponseEntity<>(
                PageHelper
                        .startPage(pageRequest.getPage(), pageRequest.getSize())
                        .doSelectPageInfo(
                                () -> sagaMapper.fulltextSearch(code, description, service, param)), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<SagaWithTask> query(Long id) {
        SagaDTO saga = sagaMapper.selectByPrimaryKey(id);
        if (saga == null) {
            throw new CommonException("error.saga.notExist");
        }
        SagaWithTask dto = new SagaWithTask(saga.getId(), saga.getCode(), saga.getDescription(), saga.getInputSchema(), saga.getService());
        SagaTaskDTO query = new SagaTaskDTO();
        query.setSagaCode(saga.getCode());
        query.setIsEnabled(true);
        List<List<SagaTask>> list = new ArrayList<>(
                sagaTaskMapper.select(query).stream()
                        .map(t -> modelMapper.map(t, SagaTask.class))
                        .collect(groupingBy(SagaTask::getSeq)).values().stream()
                        .sorted((List<SagaTask> list1, List<SagaTask> list2) -> {
                            SagaTask o1 = list1.get(0);
                            SagaTask o2 = list2.get(0);
                            return o1.getSeq().compareTo(o2.getSeq());
                        }).collect(Collectors.toList()));
        try {
            for (int i = 0; i < list.size(); i++) {
                if (i == 0) {
                    list.get(0).forEach(t -> t.setInputSchema(saga.getInputSchema()));
                } else {
                    List<JsonMerge> mergeDTOS = list.get(i - 1).stream()
                            .filter(t -> !StringUtils.isEmpty(t.getCode()) && !StringUtils.isEmpty(t.getOutputSchema()))
                            .map(t -> new JsonMerge(t.getCode(), t.getOutputSchema())).collect(Collectors.toList());
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
        SagaDTO saga = sagaMapper.selectByPrimaryKey(id);
        if (saga == null) {
            throw new CommonException("error.saga.notExist");
        }
        SagaTaskDTO sagaTask = new SagaTaskDTO();
        sagaTask.setIsEnabled(true);
        sagaTask.setSagaCode(saga.getCode());
        List<SagaTaskDTO> sagaTasks = sagaTaskMapper.select(sagaTask);
        if (!sagaTasks.isEmpty()) {
            throw new CommonException("error.saga.deleteWhenTaskExist");
        }
        sagaMapper.deleteByPrimaryKey(id);
    }
}
