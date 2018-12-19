package io.choerodon.asgard.api.service;

import java.util.Set;

import org.springframework.http.ResponseEntity;

import io.choerodon.asgard.api.dto.SagaTaskInstanceDTO;
import io.choerodon.asgard.api.dto.SagaTaskInstanceInfoDTO;
import io.choerodon.asgard.api.dto.SagaTaskInstanceStatusDTO;
import io.choerodon.asgard.saga.dto.PollBatchDTO;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

public interface SagaTaskInstanceService {

    Set<SagaTaskInstanceDTO> pollBatch(PollBatchDTO pollBatchDTO);

    void updateStatus(SagaTaskInstanceStatusDTO statusDTO);

    void unlockByInstance(String instance);

    void retry(long id);

    void unlockById(long id);

    ResponseEntity<Page<SagaTaskInstanceInfoDTO>> pageQuery(PageRequest pageRequest, String sagaInstanceCode,
                                                            String status, String taskInstanceCode,
                                                            String params, String level, Long sourceId);
}
