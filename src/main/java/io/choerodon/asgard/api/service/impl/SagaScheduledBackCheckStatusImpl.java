package io.choerodon.asgard.api.service.impl;

import io.choerodon.asgard.config.AsgardProperties;
import io.choerodon.asgard.infra.mapper.SagaInstanceMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class SagaScheduledBackCheckStatusImpl {


    private ScheduledExecutorService scheduledExecutorService;

    private SagaInstanceMapper instanceMapper;

    private AsgardProperties asgardProperties;

    public SagaScheduledBackCheckStatusImpl(@Qualifier("sagaBackCheckScheduledService") ScheduledExecutorService service,
                                            SagaInstanceMapper instanceMapper,
                                            AsgardProperties asgardProperties) {
        this.scheduledExecutorService = service;
        this.instanceMapper = instanceMapper;
        this.asgardProperties = asgardProperties;
    }

    @PostConstruct
    public void backCheck() {
        scheduledExecutorService.scheduleWithFixedDelay(() -> {
        }, 20000, asgardProperties.getSaga().getBackCheckIntervalMs(), TimeUnit.MILLISECONDS);
    }


}
