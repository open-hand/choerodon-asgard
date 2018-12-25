package io.choerodon.asgard.api.service;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import io.choerodon.asgard.api.dto.SagaInstanceDTO;
import io.choerodon.asgard.api.dto.SagaInstanceDetailsDTO;
import io.choerodon.asgard.api.dto.StartInstanceDTO;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

public interface SagaInstanceService {

    ResponseEntity<SagaInstanceDTO> start(StartInstanceDTO dto);

    ResponseEntity<Page<SagaInstanceDTO>> pageQuery(PageRequest pageRequest, String sagaCode,
                                                    String status, String refType,
                                                    String refId, String params,String level,Long sourceId);

    ResponseEntity<String> query(Long id);

    SagaInstanceDetailsDTO queryDetails(Long id);


    Map<String, Integer> statistics(String level, Long sourceId);

    Map<String, Object> queryFailedByDate(String beginDate, String endDate);
}