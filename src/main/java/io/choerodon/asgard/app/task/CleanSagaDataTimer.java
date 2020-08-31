package io.choerodon.asgard.app.task;

import io.choerodon.asgard.infra.dto.SagaTaskDTO;
import io.choerodon.asgard.infra.mapper.SagaInstanceMapper;
import io.choerodon.asgard.infra.mapper.SagaTaskInstanceMapper;
import io.choerodon.asgard.infra.mapper.SagaTaskMapper;
import io.choerodon.asgard.schedule.QuartzDefinition;
import io.choerodon.asgard.schedule.annotation.JobParam;
import io.choerodon.asgard.schedule.annotation.JobTask;
import io.choerodon.asgard.schedule.annotation.TimedTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class CleanSagaDataTimer {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanSagaDataTimer.class);

    private SagaInstanceMapper instanceMapper;

    private SagaTaskInstanceMapper taskInstanceMapper;

    private SagaTaskMapper sagaTaskMapper;

    public CleanSagaDataTimer(SagaInstanceMapper instanceMapper,
                              SagaTaskInstanceMapper taskInstanceMapper,
                              SagaTaskMapper sagaTaskMapper) {
        this.instanceMapper = instanceMapper;
        this.taskInstanceMapper = taskInstanceMapper;
        this.sagaTaskMapper = sagaTaskMapper;
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


    @JobTask(maxRetryCount = 3, code = "fixSagaData", description = "清理saga遗留下来的旧数据")
    @TimedTask(name = "fixSagaData", description = "清理saga遗留下来的旧数据", oneExecution = true,
            repeatCount = 0, repeatInterval = 1, repeatIntervalUnit = QuartzDefinition.SimpleRepeatIntervalUnit.HOURS, params = {})
    public void fixSagaDate(Map<String, Object> data) {
        //删除遗留的 saga Task
        SagaTaskDTO sagaTaskDTO = new SagaTaskDTO();
        sagaTaskDTO.setService("notify-service");
        sagaTaskDTO.setSagaCode("message-delete-env");
        sagaTaskMapper.delete(sagaTaskDTO);
    }


}
