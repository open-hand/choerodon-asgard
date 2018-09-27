package io.choerodon.asgard.api.service.impl;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.quartz.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import io.choerodon.asgard.api.dto.TriggerType;
import io.choerodon.asgard.api.service.QuartzJobService;
import io.choerodon.asgard.domain.QuartzTask;
import io.choerodon.core.exception.CommonException;

@Service
public class QuartzJobServiceImpl implements QuartzJobService {

    public static final String JOB_PREFIX = "asgard_job:";

    public static final String TRIGGER_PREFIX = "asgard_trigger:";

    private Scheduler scheduler;

    public QuartzJobServiceImpl(@Qualifier("Scheduler") Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void addJob(final QuartzTask task) {
        try {
            JobDetail jobDetail = JobBuilder.newJob(QuartzGenericCreateInstanceJob.class).withIdentity(JOB_PREFIX + task.getId())
                    .usingJobData("taskId", task.getId()).withDescription(task.getName()).build();
            TriggerBuilder triggerBuilder = TriggerBuilder.newTrigger().withIdentity(TRIGGER_PREFIX + task.getId());
            //若 StartTime为空 或 StartTime 小于 当前时间 则 开始于 当前时间 ; 否则 开始于 StartTime
            if (task.getStartTime() == null || task.getStartTime().getTime() < new Date().getTime()) {
                triggerBuilder.startNow();
            } else {
                triggerBuilder.startAt(task.getStartTime());
            }
            if (task.getEndTime() != null) {
                triggerBuilder.endAt(task.getEndTime());
            }
            if (TriggerType.SIMPLE.getValue().equals(task.getTriggerType())) {
                SimpleScheduleBuilder simpleScheduleBuilder = SimpleScheduleBuilder.simpleSchedule();
                if (task.getSimpleRepeatCount() == null) {
                    simpleScheduleBuilder.repeatForever();
                } else {
                    simpleScheduleBuilder.withRepeatCount(task.getSimpleRepeatCount());
                }
                Long intervalInMilliseconds = TimeUnit.valueOf(task.getSimpleRepeatIntervalUnit().toUpperCase()).toMillis(task.getSimpleRepeatInterval());
                simpleScheduleBuilder.withIntervalInMilliseconds(intervalInMilliseconds).withMisfireHandlingInstructionFireNow();
                triggerBuilder.withSchedule(simpleScheduleBuilder);
            } else {
                triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(task.getCronExpression()).withMisfireHandlingInstructionDoNothing());
            }
            if (!scheduler.isShutdown()) {
                scheduler.scheduleJob(jobDetail, triggerBuilder.build());
            }

            if (!scheduler.isStarted()) {
                scheduler.start();
            }
        } catch (SchedulerException e) {
            throw new CommonException("error.quartzJobService.addJob", e);
        }
    }

    @Override
    public void removeJob(final long taskId) {
        try {
            TriggerKey triggerKey = new TriggerKey(TRIGGER_PREFIX + taskId);
            JobKey jobKey = new JobKey(JOB_PREFIX + taskId);
            scheduler.pauseTrigger(triggerKey);
            scheduler.unscheduleJob(triggerKey);
            scheduler.pauseJob(jobKey);
            scheduler.deleteJob(jobKey);
        } catch (SchedulerException e) {
            throw new CommonException("error.scheduleTask.deleteTaskFailed", e);
        }
    }

    @Override
    public void resumeJob(final long taskId) {
        try {
            scheduler.resumeJob(new JobKey(JOB_PREFIX + taskId));
        } catch (SchedulerException e) {
            throw new CommonException("error.scheduleTask.enableTaskFailed", e);
        }
    }

    @Override
    public void pauseJob(long taskId) {
        try {
            scheduler.pauseJob(new JobKey(JOB_PREFIX + taskId));
        } catch (SchedulerException e) {
            throw new CommonException("error.scheduleTask.disableTaskFailed", e);
        }
    }

}
