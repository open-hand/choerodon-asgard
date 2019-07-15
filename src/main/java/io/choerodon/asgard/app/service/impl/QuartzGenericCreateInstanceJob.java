package io.choerodon.asgard.app.service.impl;

import io.choerodon.asgard.app.service.QuartzRealJobService;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class QuartzGenericCreateInstanceJob extends QuartzJobBean {

    @Autowired
    private QuartzRealJobService realJobService;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext)  {
        final long taskId = jobExecutionContext.getMergedJobDataMap().getLongValue("taskId");
        realJobService.triggerEvent(taskId, jobExecutionContext);
    }
}
