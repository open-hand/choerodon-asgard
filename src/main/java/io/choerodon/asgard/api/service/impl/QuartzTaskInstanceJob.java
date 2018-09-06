package io.choerodon.asgard.api.service.impl;

import io.choerodon.asgard.infra.mapper.QuartzTasKInstanceMapper;
import io.choerodon.asgard.infra.utils.SpringApplicationContextHelper;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Optional;

public class QuartzTaskInstanceJob implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        Optional.ofNullable(SpringApplicationContextHelper.getSpringFactory()).ifPresent(t -> {
            QuartzTasKInstanceMapper tasKInstanceMapper = t.getBean(QuartzTasKInstanceMapper.class);

        });

    }

}
