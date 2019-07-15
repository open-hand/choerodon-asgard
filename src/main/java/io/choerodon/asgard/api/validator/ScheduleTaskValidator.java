package io.choerodon.asgard.api.validator;

import io.choerodon.asgard.api.vo.ScheduleTask;
import io.choerodon.asgard.infra.enums.TriggerType;
import io.choerodon.core.exception.CommonException;
import org.quartz.CronExpression;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;

/**
 * @author dengyouquan
 **/
public class ScheduleTaskValidator {

    private ScheduleTaskValidator() {
    }

    public static void validatorCreate(ScheduleTask dto) {
        if (dto.getParams() == null) {
            dto.setParams(new HashMap<>(0));
        }
        if (dto.getEndTime() != null && dto.getEndTime().getTime() < new Date().getTime()) {
            throw new CommonException("error.scheduleTask.endTime.cantbefore.now");
        }
        if (TriggerType.CRON.getValue().equals(dto.getTriggerType())) {
            if (StringUtils.isEmpty(dto.getCronExpression())) {
                throw new CommonException("error.scheduleTask.cronExpressionEmpty");
            }
            if (!CronExpression.isValidExpression(dto.getCronExpression())) {
                throw new CommonException("error.scheduleTask.cronExpressionInvalid");
            }
        } else if (TriggerType.SIMPLE.getValue().equals(dto.getTriggerType())) {
            // 实际重复次数 为 前端传回重复次数（执行次数） -1
            dto.setSimpleRepeatCount(dto.getSimpleRepeatCount() - 1);

            if (dto.getSimpleRepeatInterval() == null) {
                throw new CommonException("error.scheduleTask.repeatCountOrRepeatIntervalNull");
            }
            if (dto.getSimpleRepeatIntervalUnit() == null) {
                throw new CommonException("error.scheduleTask.repeatCountOrRepeatIntervalUnitNull");
            }
        } else {
            throw new CommonException("error.scheduleTask.invalidTriggerType");
        }
    }
}
