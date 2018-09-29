package io.choerodon.asgard.api.service.impl;

import io.choerodon.asgard.api.service.QuartzRealJobService;
import io.choerodon.asgard.api.service.ScheduleTaskInstanceService;
import io.choerodon.asgard.api.service.ScheduleTaskService;
import io.choerodon.asgard.domain.QuartzMethod;
import io.choerodon.asgard.domain.QuartzTask;
import io.choerodon.asgard.domain.QuartzTaskInstance;
import io.choerodon.asgard.infra.mapper.QuartzMethodMapper;
import io.choerodon.asgard.infra.mapper.QuartzTaskInstanceMapper;
import io.choerodon.asgard.infra.mapper.QuartzTaskMapper;
import io.choerodon.asgard.infra.utils.TriggerUtils;
import io.choerodon.asgard.schedule.QuartzDefinition;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;

@Service
public class QuartzRealJobServiceImpl implements QuartzRealJobService {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuartzRealJobService.class);

    private QuartzTaskMapper taskMapper;

    private QuartzTaskInstanceMapper instanceMapper;

    private QuartzMethodMapper methodMapper;

    private ScheduleTaskService scheduleTaskService;
    private ScheduleTaskInstanceService scheduleTaskInstanceService;

    public QuartzRealJobServiceImpl(QuartzTaskMapper taskMapper,
                                    QuartzTaskInstanceMapper instanceMapper,
                                    QuartzMethodMapper methodMapper,
                                    ScheduleTaskService scheduleTaskService,
                                    ScheduleTaskInstanceService scheduleTaskInstanceService) {
        this.taskMapper = taskMapper;
        this.instanceMapper = instanceMapper;
        this.methodMapper = methodMapper;
        this.scheduleTaskService = scheduleTaskService;
        this.scheduleTaskInstanceService = scheduleTaskInstanceService;
    }

    @Override
    public void triggerEvent(final long taskId, final JobExecutionContext jobExecutionContext) {
        final QuartzTaskInstance lastInstance = instanceMapper.selectLastInstance(taskId);
        if (lastInstance != null && !QuartzDefinition.InstanceStatus.COMPLETED.name().equals(lastInstance.getStatus())) {
            if (QuartzDefinition.InstanceStatus.RUNNING.name().equals(lastInstance.getStatus())) {
                scheduleTaskInstanceService.failed(lastInstance.getId(), "定时任务未被执行");
            }
            scheduleTaskService.disable(taskId, null, true);
            return;
        }
        createInstance(taskId, lastInstance);
        if (jobExecutionContext.getNextFireTime() == null) {
            scheduleTaskService.finish(taskId);
        }
    }

    private void createInstance(long taskId, final QuartzTaskInstance lastInstance) {
        QuartzTask task = taskMapper.selectByPrimaryKey(taskId);
        if (task == null) {
            LOGGER.warn("task not exist when createInstance {}", taskId);
            return;
        }
        if (!QuartzDefinition.TaskStatus.ENABLE.name().equals(task.getStatus())) {
            LOGGER.warn("task not enable when createInstance {}", task);
            return;
        }
        QuartzMethod query = new QuartzMethod();
        query.setCode(task.getExecuteMethod());
        QuartzMethod db = methodMapper.selectOne(query);
        if (db == null) {
            LOGGER.warn("task method not exist when createInstance {}", task);
            return;
        }
        QuartzTaskInstance taskInstance = new QuartzTaskInstance();
        taskInstance.setTaskId(taskId);
        taskInstance.setTaskName(task.getName());
        taskInstance.setPlannedStartTime(new Date());
        taskInstance.setExecuteMethod(task.getExecuteMethod());
        taskInstance.setRetriedCount(0);
        if (lastInstance != null) {
            taskInstance.setActualLastTime(lastInstance.getActualStartTime());
            if (lastInstance.getExecuteResult() != null) {
                taskInstance.setExecuteParams(lastInstance.getExecuteResult());
            } else {
                taskInstance.setExecuteParams(task.getExecuteParams());
            }
        } else {
            taskInstance.setExecuteParams(task.getExecuteParams());
        }
        if (StringUtils.isEmpty(taskInstance.getExecuteParams())) {
            taskInstance.setExecuteParams("{}");
        }
        taskInstance.setStatus(QuartzDefinition.InstanceStatus.RUNNING.name());
        taskInstance.setPlannedNextTime(TriggerUtils.getNextFireTime(task, taskInstance));
        taskInstance.setMaxRetryCount(db.getMaxRetryCount());
        if (instanceMapper.insert(taskInstance) != 1) {
            LOGGER.warn("taskInstance insert error when createInstance {}", task);
        }
    }

}
