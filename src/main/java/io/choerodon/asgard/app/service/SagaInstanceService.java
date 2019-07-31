package io.choerodon.asgard.app.service;

import com.github.pagehelper.PageInfo;
import io.choerodon.asgard.api.vo.SagaInstance;
import io.choerodon.asgard.api.vo.SagaInstanceDetails;
import io.choerodon.asgard.api.vo.StartInstance;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface SagaInstanceService {

    ResponseEntity<SagaInstance> start(StartInstance dto);

    ResponseEntity<SagaInstance> preCreate(StartInstance dto);

    void confirm(String uuid, String payloadJson, String refType, String refId);

    void cancel(String uuid);


    ResponseEntity<PageInfo<SagaInstanceDetails>> pageQuery(int page, int size, String sagaCode,
                                                     String status, String refType,
                                                     String refId, String params, String level, Long sourceId);

    ResponseEntity<String> query(Long id);

    SagaInstanceDetails queryDetails(Long id);


    Map<String, Integer> statistics(String level, Long sourceId);

    Map<String, Object> queryFailedByDate(String beginDate, String endDate);
}