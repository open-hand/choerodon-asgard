package io.choerodon.asgard.api.service;

import io.choerodon.asgard.api.dto.SagaTaskInstanceDTO;
import io.choerodon.asgard.api.dto.SagaTaskInstanceStatusDTO;
import io.choerodon.asgard.saga.dto.PollBatchDTO;

import java.util.Set;

public interface SagaTaskInstanceService {

    Set<SagaTaskInstanceDTO>  pollBatch(PollBatchDTO pollBatchDTO);

    SagaTaskInstanceDTO updateStatus(SagaTaskInstanceStatusDTO statusDTO);

    void unlockByInstance(String instance);

    void retry(long id);

    void unlockById(long id);

}
