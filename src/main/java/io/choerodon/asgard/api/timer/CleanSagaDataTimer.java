package io.choerodon.asgard.api.timer;

import io.choerodon.asgard.config.AsgardProperties;
import io.choerodon.asgard.infra.mapper.SagaInstanceMapper;
import io.choerodon.asgard.infra.mapper.SagaTaskInstanceMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CleanSagaDataTimer {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanSagaDataTimer.class);

    private SagaInstanceMapper instanceMapper;

    private SagaTaskInstanceMapper taskInstanceMapper;

    private ScheduledExecutorService scheduledExecutorService;

    private AsgardProperties asgardProperties;

    private final long fromNowSeconds;

    public CleanSagaDataTimer(ScheduledExecutorService scheduledExecutorService,
                              SagaInstanceMapper instanceMapper,
                              SagaTaskInstanceMapper taskInstanceMapper,
                              AsgardProperties asgardProperties) {
        this.instanceMapper = instanceMapper;
        this.taskInstanceMapper = taskInstanceMapper;
        this.scheduledExecutorService = scheduledExecutorService;
        this.asgardProperties = asgardProperties;
        this.fromNowSeconds = asgardProperties.getSaga().getCleanSagaBeforeNowMinutes() * 60L;
    }

    @PostConstruct
    public void cleanPostConstruct() {
        if (asgardProperties.getSaga().isCleanEnabled()) {
            scheduledExecutorService.scheduleWithFixedDelay(this::clean, 1,
                    asgardProperties.getSaga().getCleanExecuteIntervalMinutes(), TimeUnit.MINUTES);
        }
    }

    private void clean() {
        List<Long> completedAndTimeOutInstanceIds = instanceMapper.selectCompletedIdByDate(fromNowSeconds, new Date());
        int instanceNum = instanceMapper.deleteBatchByIds(completedAndTimeOutInstanceIds);
        LOGGER.info("delete out-of-date data from ASGARD_SAGA_INSTANCE, num: {}, ids : {}", instanceNum, completedAndTimeOutInstanceIds);

        List<Long> completedAndTimeOutTaskInstanceIds = taskInstanceMapper.selectCompletedIdByDate(fromNowSeconds, new Date());
        int taskInstanceNum = taskInstanceMapper.deleteBatchByIds(completedAndTimeOutTaskInstanceIds);
        LOGGER.info("delete out-of-date data from ASGARD_SAGA_TASK_INSTANCE, num: {}, ids : {}", taskInstanceNum, completedAndTimeOutTaskInstanceIds);
    }


}
