package io.choerodon.asgard.api.service;

import io.choerodon.asgard.api.dto.SagaInstanceDTO;
import io.choerodon.asgard.api.dto.SagaInstanceDetailsDTO;
import io.choerodon.asgard.api.dto.StartInstanceDTO;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface SagaInstanceService {

    ResponseEntity<SagaInstanceDTO> start(StartInstanceDTO dto);

    ResponseEntity<SagaInstanceDTO> preCreate(StartInstanceDTO dto);

    void confirm(String uuid, String payloadJson, String refType, String refId);

    void cancel(String uuid);


    ResponseEntity<Page<SagaInstanceDTO>> pageQuery(PageRequest pageRequest, String sagaCode,
                                                    String status, String refType,
                                                    String refId, String params, String level, Long sourceId);

    ResponseEntity<String> query(Long id);

    SagaInstanceDetailsDTO queryDetails(Long id);


    Map<String, Integer> statistics(String level, Long sourceId);

    Map<String, Object> queryFailedByDate(String beginDate, String endDate);
}