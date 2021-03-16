package io.choerodon.asgard.app.service;

import io.choerodon.asgard.api.vo.*;
import io.choerodon.asgard.infra.dto.SagaInstanceDTO;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface SagaInstanceService {

    ResponseEntity<SagaInstance> start(StartInstance dto);

    ResponseEntity<SagaInstance> preCreate(StartInstance dto);

    void confirm(String uuid, String payloadJson, String refType, String refId);

    void cancel(String uuid);


    ResponseEntity<Page<SagaInstanceDetails>> pageQuery(PageRequest pageable, String sagaCode, String status, String refType, String refId, String params, String level, Long sourceId, Long id);

    SagaWithTaskInstance query(Long id);

    SagaInstanceDetails queryDetails(Long id);

    Map<String, Integer> statistics(String level, Long sourceId);

    List<SagaInstanceFailureVO> statisticsFailure(String level, Long sourceId, Integer date);

    Page<SagaInstanceDTO> statisticsFailureList(String level, Long sourceId, Integer date, PageRequest pageable);

    Map<String, Object> queryFailedByDate(String beginDate, String endDate);

    List<SagaInstanceDetails> queryByRefTypeAndRefIds(String refType, List<String> refIds, String sagaCode);

}