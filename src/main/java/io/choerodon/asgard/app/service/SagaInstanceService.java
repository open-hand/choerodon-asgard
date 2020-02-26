package io.choerodon.asgard.app.service;

import com.github.pagehelper.PageInfo;
import io.choerodon.asgard.api.vo.SagaInstance;
import io.choerodon.asgard.api.vo.SagaInstanceDetails;
import io.choerodon.asgard.api.vo.SagaInstanceFailureVO;
import io.choerodon.asgard.api.vo.StartInstance;
import io.choerodon.asgard.infra.dto.SagaInstanceDTO;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface SagaInstanceService {

    ResponseEntity<SagaInstance> start(StartInstance dto);

    ResponseEntity<SagaInstance> preCreate(StartInstance dto);

    void confirm(String uuid, String payloadJson, String refType, String refId);

    void cancel(String uuid);


    ResponseEntity<PageInfo<SagaInstanceDetails>> pageQuery(Pageable pageable, String sagaCode, String status, String refType, String refId, String params, String level, Long sourceId);

    ResponseEntity<String> query(Long id);

    SagaInstanceDetails queryDetails(Long id);


    Map<String, Integer> statistics(String level, Long sourceId);

    List<SagaInstanceFailureVO> statisticsFailure(String level, Long sourceId, Integer date);

    PageInfo<SagaInstanceDTO> statisticsFailureList(String level, Long sourceId, Integer date, Pageable pageable);

    Map<String, Object> queryFailedByDate(String beginDate, String endDate);
}