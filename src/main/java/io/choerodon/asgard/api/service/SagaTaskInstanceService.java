package io.choerodon.asgard.api.service;

import io.choerodon.asgard.api.dto.PollBatchDTO;
import io.choerodon.asgard.api.dto.SagaTaskInstanceDTO;
import io.choerodon.asgard.api.dto.SagaTaskInstanceStatusDTO;

import java.util.List;

public interface SagaTaskInstanceService {

    List<SagaTaskInstanceDTO> pollBatch(PollBatchDTO pollBatchDTO);

    void updateStatus(SagaTaskInstanceStatusDTO statusDTO);

    void unlockByInstance(String instance);

}
