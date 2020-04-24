package io.choerodon.asgard.app.service.impl;

import io.choerodon.asgard.api.vo.ScheduleTask;
import io.choerodon.asgard.app.eventhandler.SagaInstanceEventPublisher;
import io.choerodon.asgard.app.service.QuartzRealJobService;
import io.choerodon.asgard.app.service.ScheduleTaskInstanceService;
import io.choerodon.asgard.app.service.ScheduleTaskService;
import io.choerodon.asgard.infra.dto.QuartzMethodDTO;
import io.choerodon.asgard.infra.dto.QuartzTaskDTO;
import io.choerodon.asgard.infra.dto.QuartzTaskInstanceDTO;
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
    private SagaInstanceEventPublisher sagaInstanceEventPublisher;

    public QuartzRealJobServiceImpl(QuartzTaskMapper taskMapper,
                                    QuartzTaskInstanceMapper instanceMapper,
                                    QuartzMethodMapper methodMapper,
                                    ScheduleTaskService scheduleTaskService,
                                    ScheduleTaskInstanceService scheduleTaskInstanceService,
                                    SagaInstanceEventPublisher sagaInstanceEventPublisher) {
        this.taskMapper = taskMapper;
        this.instanceMapper = instanceMapper;
        this.methodMapper = methodMapper;
        this.scheduleTaskService = scheduleTaskService;
        this.scheduleTaskInstanceService = scheduleTaskInstanceService;
        this.sagaInstanceEventPublisher = sagaInstanceEventPublisher;
    }

    @Override
    public void triggerEvent(final long taskId, final JobExecutionContext jobExecutionContext) {
        final QuartzTaskInstanceDTO lastInstance = instanceMapper.selectLastInstance(taskId);
        QuartzTaskDTO task = taskMapper.selectByPrimaryKey(taskId);
        String strategy = task.getExecuteStrategy();
        //任务执行策略为STOP
        boolean isStop = ScheduleTask.TriggerEventStrategy.STOP.name().equalsIgnoreCase(strategy);
        boolean isLastInstanceCompleted = false;
        // 若最近执行记录状态为：Running，则将最近执行记录置为失败
        if (lastInstance != null) {
            isLastInstanceCompleted = !QuartzDefinition.InstanceStatus.COMPLETED.name().equals(lastInstance.getStatus());
        }
        if (isStop && isLastInstanceCompleted) {
            if (QuartzDefinition.InstanceStatus.RUNNING.name().equals(lastInstance.getStatus())) {
                scheduleTaskInstanceService.failed(lastInstance.getId(), "定时任务未被执行");
            }
            scheduleTaskService.disable(taskId, null, true);
            return;
        }
        createInstance(taskId, lastInstance);
        //若无下次执行计划，则结束任务
        if (jobExecutionContext.getNextFireTime() == null) {
            scheduleTaskService.finish(taskId);
        }
    }

    private void createInstance(long taskId, final QuartzTaskInstanceDTO lastInstance) {
        QuartzTaskDTO task = taskMapper.selectByPrimaryKey(taskId);
        if (task == null) {
            LOGGER.warn("task not exist when createInstance {}", taskId);
            return;
        }
        if (!QuartzDefinition.TaskStatus.ENABLE.name().equals(task.getStatus())) {
            LOGGER.warn("task not enable when createInstance {}", task);
            return;
        }
        QuartzMethodDTO query = new QuartzMethodDTO();
        query.setCode(task.getExecuteMethod());
        QuartzMethodDTO db = methodMapper.selectOne(query);
        if (db == null) {
            LOGGER.warn("task method not exist when createInstance {}", task);
            return;
        }
        QuartzTaskInstanceDTO taskInstance = new QuartzTaskInstanceDTO();
        taskInstance.setTaskId(taskId);
        taskInstance.setTaskName(task.getName());
        taskInstance.setPlannedStartTime(new Date());
        taskInstance.setExecuteMethod(task.getExecuteMethod());
        taskInstance.setRetriedCount(0);
        //设置taskInstance的层级
        taskInstance.setLevel(task.getLevel());
        taskInstance.setSourceId(task.getSourceId());
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
        } else {
            sagaInstanceEventPublisher.quartzInstanceEvent(db.getService());
        }
    }

}
