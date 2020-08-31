package io.choerodon.asgard.app.task;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.asgard.infra.dto.SagaTaskDTO;
import io.choerodon.asgard.infra.mapper.SagaTaskMapper;
import io.choerodon.asgard.schedule.QuartzDefinition;
import io.choerodon.asgard.schedule.annotation.JobTask;
import io.choerodon.asgard.schedule.annotation.TimedTask;

/**
 * Created by wangxiang on 2020/8/31
 */
@Component
public class TaskConfig {


    @Autowired
    private SagaTaskMapper sagaTaskMapper;

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
