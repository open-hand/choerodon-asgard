package io.choerodon.asgard.app.service;

import io.choerodon.asgard.infra.dto.SagaTaskDTO;

import java.util.List;

public interface SagaTaskService {

    void createSagaTaskList(List<SagaTaskDTO> sagaTaskList, String service);

}
