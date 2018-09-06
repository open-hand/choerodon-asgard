package io.choerodon.asgard.api.service.impl;

import io.choerodon.asgard.api.service.QuartzRealJobService;
import io.choerodon.asgard.infra.utils.SpringApplicationContextHelper;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Optional;

public class QuartzGenericCreateInstanceJob implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        final long taskId = jobExecutionContext.getMergedJobDataMap().getLongValue("taskId");
        Optional.ofNullable(SpringApplicationContextHelper.getSpringFactory()).ifPresent(t -> {
            QuartzRealJobService realJobService = t.getBean(QuartzRealJobService.class);
            realJobService.triggerEvent(taskId);
        });

    }

}
