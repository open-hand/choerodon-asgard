package io.choerodon.asgard.api.service.impl;

import io.choerodon.asgard.api.pojo.TriggerType;
import io.choerodon.asgard.api.service.QuartzJobService;
import io.choerodon.asgard.domain.QuartzTask;
import io.choerodon.core.exception.CommonException;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class QuartzJobServiceImpl implements QuartzJobService {

    private static final String JOB_PREFIX = "job:";

    private static final String TRIGGER_PREFIX = "trigger:";

    private Scheduler scheduler;

    public QuartzJobServiceImpl(@Qualifier("Scheduler") Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void addJob(final QuartzTask task) {
        try {
            JobDetail jobDetail = JobBuilder.newJob(QuartzGenericCreateInstanceJob.class).withIdentity(JOB_PREFIX + task.getId())
                    .usingJobData("taskId", task.getId()).withDescription(task.getName()).build();
            TriggerBuilder triggerBuilder = TriggerBuilder.newTrigger().withIdentity(TRIGGER_PREFIX + task.getId());
            if (task.getStartTime() == null) {
                triggerBuilder.startNow();
            } else {
                triggerBuilder.startAt(task.getStartTime());
            }
            if (task.getEndTime() != null) {
                triggerBuilder.endAt(task.getEndTime());
            }
            if (TriggerType.SIMPLE.name().equals(task.getTriggerType())) {
                SimpleScheduleBuilder simpleScheduleBuilder = SimpleScheduleBuilder.simpleSchedule();
                if (task.getSimpleRepeatCount() == null) {
                    simpleScheduleBuilder.repeatForever();
                } else {
                    simpleScheduleBuilder.withRepeatCount(task.getSimpleRepeatCount());
                }
                simpleScheduleBuilder.withIntervalInMilliseconds(task.getSimpleRepeatInterval()).withMisfireHandlingInstructionFireNow();
                triggerBuilder.withSchedule(simpleScheduleBuilder);
            } else {
                triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(task.getCronExpression()));
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
    public boolean checkJobIsExists(final long taskId) {
        try {
            return scheduler.checkExists(new JobKey(JOB_PREFIX + taskId));
        } catch (SchedulerException e) {
            throw new CommonException("error.quartzJobService.checkJobIsExists", e);
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
