package io.choerodon.asgard.infra.utils;

import io.choerodon.asgard.api.dto.TriggerType;
import io.choerodon.asgard.domain.QuartzTask;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.scheduling.support.SimpleTriggerContext;

import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class TriggerUtils {

    private TriggerUtils() {

    }

    public static Date getNextFireTime(final QuartzTask task) {
        Date nextDate;
        if (TriggerType.CRON.name().equals(task.getTriggerType())) {
            CronTrigger c = new CronTrigger(task.getCronExpression(), TimeZone.getDefault());
            SimpleTriggerContext t = new SimpleTriggerContext();
            t.update(new Date(), new Date(), new Date());
            nextDate = c.nextExecutionTime(t);
        } else {
            final PeriodicTrigger periodicTrigger = new PeriodicTrigger(task.getSimpleRepeatInterval(), TimeUnit.MILLISECONDS);
            SimpleTriggerContext t = new SimpleTriggerContext();
            t.update(new Date(), new Date(), new Date());
            nextDate = periodicTrigger.nextExecutionTime(t);
        }
        if (task.getEndTime() == null) {
            return nextDate;
        }
        return task.getEndTime().compareTo(nextDate) >= 0 ? nextDate : null;
    }


}
