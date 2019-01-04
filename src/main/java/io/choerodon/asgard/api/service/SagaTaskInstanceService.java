package io.choerodon.asgard.api.service;

import io.choerodon.asgard.api.dto.SagaTaskInstanceDTO;
import io.choerodon.asgard.api.dto.SagaTaskInstanceInfoDTO;
import io.choerodon.asgard.api.dto.SagaTaskInstanceStatusDTO;
import io.choerodon.asgard.saga.dto.PollSagaTaskInstanceDTO;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.springframework.http.ResponseEntity;

import java.util.Set;

public interface SagaTaskInstanceService {

    Set<SagaTaskInstanceDTO> pollBatch(final PollSagaTaskInstanceDTO pollBatchDTO);

    void updateStatus(SagaTaskInstanceStatusDTO statusDTO);

    SagaTaskInstanceDTO query(long id);

    void unlockByInstance(String instance);

    void retry(long id);

    void unlockById(long id);

    void forceFailed(long id);

    ResponseEntity<Page<SagaTaskInstanceInfoDTO>> pageQuery(PageRequest pageRequest, String sagaInstanceCode,
                                                            String status, String taskInstanceCode,
                                                            String params, String level, Long sourceId);
}
