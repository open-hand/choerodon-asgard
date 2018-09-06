package io.choerodon.asgard.api.service.impl;

import io.choerodon.asgard.api.service.QuartzJobService;
import io.choerodon.asgard.domain.QuartzTask;
import org.quartz.Scheduler;
import org.springframework.stereotype.Service;

@Service
public class QuartzJobServiceImpl implements QuartzJobService {

    private Scheduler scheduler;

    @Override
    public void addSimpleJob(QuartzTask task) {

    }

    @Override
    public void addCronJob(QuartzTask task) {

    }

    @Override
    public void removeJob(QuartzTask task) {

    }

    @Override
    public void resumeJob(QuartzTask task) {

    }

    @Override
    public void checkJobIsExists(QuartzTask task) {

    }

    @Override
    public void pauseJob(long taskId) {

    }
}
