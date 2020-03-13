package io.choerodon.asgard.infra.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import io.choerodon.asgard.infra.dto.QuartzTaskInstanceDTO;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.scheduling.support.SimpleTriggerContext;

import io.choerodon.asgard.infra.enums.TriggerType;
import io.choerodon.asgard.infra.dto.QuartzTaskDTO;
import io.choerodon.core.exception.CommonException;

public class TriggerUtils {

    private TriggerUtils() {

    }

    public static Date getNextFireTime(final QuartzTaskDTO task, final QuartzTaskInstanceDTO taskInstance) {
        Date nextDate;
        Date lastFiredTime = taskInstance.getPlannedStartTime();
        if (TriggerType.CRON.getValue().equals(task.getTriggerType())) {
            String cron = task.getCronExpression();
            if (cron.split(" ").length == 7) {
                cron = cron.substring(0, cron.lastIndexOf(' '));
            }
            CronTrigger c = new CronTrigger(cron, TimeZone.getDefault());
            SimpleTriggerContext t = new SimpleTriggerContext();
            t.update(lastFiredTime, lastFiredTime, lastFiredTime);
            nextDate = c.nextExecutionTime(t);
        } else {
            Long intervalInMilliseconds = TimeUnit.valueOf(task.getSimpleRepeatIntervalUnit().toUpperCase()).toMillis(task.getSimpleRepeatInterval());
            final PeriodicTrigger periodicTrigger = new PeriodicTrigger(intervalInMilliseconds, TimeUnit.MILLISECONDS);
            SimpleTriggerContext t = new SimpleTriggerContext();
            t.update(lastFiredTime, lastFiredTime, lastFiredTime);
            nextDate = periodicTrigger.nextExecutionTime(t);
        }
        if (task.getEndTime() == null) {
            return nextDate;
        }
        return task.getEndTime().compareTo(nextDate) >= 0 ? nextDate : null;
    }

    public static List<String> getRecentThree(final String cron) {
        CronTriggerImpl cronTriggerImpl = new CronTriggerImpl();
        try {
            cronTriggerImpl.setCronExpression(cron);
        } catch (ParseException e) {
            throw new CommonException("error.cron.parse");
        }
        List<Date> dates = org.quartz.TriggerUtils.computeFireTimes(cronTriggerImpl, null, 3);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dates.stream()
                .map(format::format).collect(Collectors.toList());
    }

    public static Date getStartTime(final String cron) {
        CronTriggerImpl cronTriggerImpl = new CronTriggerImpl();
        try {
            cronTriggerImpl.setCronExpression(cron);
        } catch (ParseException e) {
            throw new CommonException("error.cron.parse");
        }
        return org.quartz.TriggerUtils.computeFireTimes(cronTriggerImpl, null, 1).get(0);
    }

}
