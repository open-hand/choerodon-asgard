package io.choerodon.asgard.api.service.impl;

import io.choerodon.asgard.api.service.QuartzJobService;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class QuartzJobServiceImpl implements QuartzJobService {

    private Scheduler scheduler;

    @Autowired
    public QuartzJobServiceImpl(@Qualifier("Scheduler") Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void addSimpleJob() {

    }

    @Override
    public void addCronJob() {

    }


}
