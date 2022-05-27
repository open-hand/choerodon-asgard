package io.choerodon.asgard.app.task;

import java.util.Calendar;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.choerodon.asgard.api.vo.SysSettingVO;
import io.choerodon.asgard.infra.feign.IamFeignClient;
import io.choerodon.asgard.infra.mapper.JsonDataMapper;
import io.choerodon.asgard.infra.mapper.SagaInstanceMapper;
import io.choerodon.asgard.infra.mapper.SagaTaskInstanceMapper;
import io.choerodon.asgard.schedule.annotation.JobTask;
import io.choerodon.asgard.schedule.annotation.TimedTask;
import io.choerodon.asgard.schedule.enums.TriggerTypeEnum;

@Component
public class CleanSagaInstanceTimer {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanSagaInstanceTimer.class);

    private SagaInstanceMapper instanceMapper;

    private SagaTaskInstanceMapper taskInstanceMapper;

    private JsonDataMapper jsonDataMapper;

    private IamFeignClient iamFeignClient;


    public CleanSagaInstanceTimer(SagaInstanceMapper instanceMapper,
                                  SagaTaskInstanceMapper taskInstanceMapper,
                                  JsonDataMapper jsonDataMapper,
                                  IamFeignClient iamFeignClient) {
        this.instanceMapper = instanceMapper;
        this.taskInstanceMapper = taskInstanceMapper;
        this.jsonDataMapper = jsonDataMapper;
        this.iamFeignClient = iamFeignClient;
    }

    public void cleanSagaInstance() {

        SysSettingVO setting = iamFeignClient.getSetting();

        if (setting == null || Boolean.FALSE.equals(setting.getAutoCleanSagaInstance())) {
            return;
        }
        if (setting.getAutoCleanSagaInstanceInterval() == null || setting.getAutoCleanSagaInstanceInterval() < 1) {
            return;
        }
        Calendar time = Calendar.getInstance();
        time.add(Calendar.DAY_OF_MONTH, -setting.getAutoCleanSagaInstanceInterval());
        // 数据量太大 按天遍历删除
        for (int i = 0; i < setting.getAutoCleanSagaInstanceInterval(); i++) {
            LOGGER.info("=========clean saga instance on :{}", time.getTime());
            instanceMapper.deleteByOptions(time.getTime(), 1, Boolean.TRUE.equals(true));
            time.add(Calendar.DAY_OF_MONTH, -1);
        }
        instanceMapper.deleteByOptions(time.getTime(), null, Boolean.TRUE.equals(true));
    }

    @JobTask(code = "cleanSagaInstance", maxRetryCount = 0,
            description = "清理已完成的saga实例和task实例", enableTransaction = false)
    @TimedTask(name = "cleanSagaInstance", description = "清理saga instance", oneExecution = false, params = {},
            triggerType = TriggerTypeEnum.CRON_TRIGGER, cronExpression = "0 0 2 * * ? *")
    public void cleanSagaInstance(Map<String, Object> data) {
        cleanSagaInstance();
    }


}
