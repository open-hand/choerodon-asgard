package io.choerodon.asgard.api.service.impl;

import io.choerodon.asgard.api.service.QuartzRealJobService;
import io.choerodon.asgard.api.service.ScheduleTaskService;
import io.choerodon.asgard.domain.QuartzMethod;
import io.choerodon.asgard.domain.QuartzTasKInstance;
import io.choerodon.asgard.domain.QuartzTask;
import io.choerodon.asgard.infra.mapper.QuartzMethodMapper;
import io.choerodon.asgard.infra.mapper.QuartzTaskInstanceMapper;
import io.choerodon.asgard.infra.mapper.QuartzTaskMapper;
import io.choerodon.asgard.infra.utils.TriggerUtils;
import io.choerodon.asgard.schedule.QuartzDefinition;
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

    public QuartzRealJobServiceImpl(QuartzTaskMapper taskMapper,
                                    QuartzTaskInstanceMapper instanceMapper,
                                    QuartzMethodMapper methodMapper,
                                    ScheduleTaskService scheduleTaskService) {
        this.taskMapper = taskMapper;
        this.instanceMapper = instanceMapper;
        this.methodMapper = methodMapper;
        this.scheduleTaskService = scheduleTaskService;
    }

    @Override
    public void triggerEvent(long taskId) {
        final QuartzTasKInstance lastInstance = instanceMapper.selectLastInstance(taskId);
        if (lastInstance != null && !QuartzDefinition.InstanceStatus.COMPLETED.name().equals(lastInstance.getStatus())) {
            scheduleTaskService.disable(taskId, null, true);
            return;
        }
        createInstance(taskId, lastInstance);
    }


    private void createInstance(long taskId, final QuartzTasKInstance lastInstance) {
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
        query.setMethod(task.getExecuteMethod());
        QuartzMethod db = methodMapper.selectOne(query);
        if (db == null) {
            LOGGER.warn("task method not exist when createInstance {}", task);
            return;
        }
        QuartzTasKInstance tasKInstance = new QuartzTasKInstance();
        tasKInstance.setTaskId(taskId);
        tasKInstance.setPlannedStartTime(new Date());
        tasKInstance.setExecuteMethod(task.getExecuteMethod());
        tasKInstance.setRetriedCount(0);
        if (lastInstance != null) {
            tasKInstance.setActualLastTime(lastInstance.getActualStartTime());
            tasKInstance.setExecuteParams(lastInstance.getExecuteResult());
        } else {
            tasKInstance.setExecuteParams(task.getExecuteParams());
        }
        if (StringUtils.isEmpty(tasKInstance.getExecuteParams())) {
            tasKInstance.setExecuteParams("{}");
        }
        tasKInstance.setStatus(QuartzDefinition.InstanceStatus.RUNNING.name());
        tasKInstance.setPlannedNextTime(TriggerUtils.getNextFireTime(task));
        tasKInstance.setMaxRetryCount(db.getMaxRetryCount());
        if (instanceMapper.insert(tasKInstance) != 1) {
            LOGGER.warn("taskInstance insert error when createInstance {}", task);
        }
    }

}
