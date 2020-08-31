package io.choerodon.asgard.app.task;

import io.choerodon.asgard.infra.mapper.SagaInstanceMapper;
import io.choerodon.asgard.infra.mapper.SagaTaskInstanceMapper;
import io.choerodon.asgard.infra.mapper.SagaTaskMapper;
import io.choerodon.asgard.schedule.annotation.JobParam;
import io.choerodon.asgard.schedule.annotation.JobTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class CleanSagaDataTimer {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanSagaDataTimer.class);

    private SagaInstanceMapper instanceMapper;

    private SagaTaskInstanceMapper taskInstanceMapper;


    public CleanSagaDataTimer(SagaInstanceMapper instanceMapper,
                              SagaTaskInstanceMapper taskInstanceMapper) {
        this.instanceMapper = instanceMapper;
        this.taskInstanceMapper = taskInstanceMapper;
    }

    @JobTask(code = "cleanCompetedSagaData", maxRetryCount = 0,
            description = "清理已完成的saga实例和task实例",
            params = @JobParam(name = "minutesAgo", type = Integer.class, defaultValue = "10080", description = "清理多久之前的消息(分钟)"))
    public void clean(Map<String, Object> data) {
        Object minutesAgo = data.get("minutesAgo");
        if (minutesAgo == null) {
            LOGGER.warn("error.cleanSagaDataTimer.fromNowMinutesNull");
            return;
        }
        long fromNowSeconds = (Integer) minutesAgo * 60L;
        List<Long> completedAndTimeOutInstanceIds = instanceMapper.selectCompletedIdByDate(fromNowSeconds, new Date());
        if (!completedAndTimeOutInstanceIds.isEmpty()) {
            int instanceNum = instanceMapper.deleteBatchByIds(completedAndTimeOutInstanceIds);
            LOGGER.info("delete out-of-date data from ASGARD_SAGA_INSTANCE, num: {}, ids : {}", instanceNum, completedAndTimeOutInstanceIds);
        }

        List<Long> completedAndTimeOutTaskInstanceIds = taskInstanceMapper.selectCompletedIdByDate(fromNowSeconds, new Date());
        if (!completedAndTimeOutTaskInstanceIds.isEmpty()) {
            int taskInstanceNum = taskInstanceMapper.deleteBatchByIds(completedAndTimeOutTaskInstanceIds);
            LOGGER.info("delete out-of-date data from ASGARD_SAGA_TASK_INSTANCE, num: {}, ids : {}", taskInstanceNum, completedAndTimeOutTaskInstanceIds);
        }
    }


}
