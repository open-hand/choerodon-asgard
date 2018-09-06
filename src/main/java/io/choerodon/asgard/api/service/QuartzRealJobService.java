package io.choerodon.asgard.api.service;

public interface QuartzRealJobService {

    void triggerEvent(long taskId);

}
