package io.choerodon.asgard.api.service;

import org.quartz.JobExecutionContext;

public interface QuartzRealJobService {

    void triggerEvent(long taskId, JobExecutionContext jobExecutionContext);

}
