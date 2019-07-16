package io.choerodon.asgard.app.service;

import org.quartz.JobExecutionContext;

public interface QuartzRealJobService {

    void triggerEvent(long taskId, JobExecutionContext jobExecutionContext);

}
