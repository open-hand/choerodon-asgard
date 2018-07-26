package io.choerodon.asgard.api.service;

import io.choerodon.asgard.domain.SagaTask;

import java.util.List;

public interface SagaTaskService {

    void createSagaTaskList(List<SagaTask> sagaTaskList, String service);

}
