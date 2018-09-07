package io.choerodon.asgard.api.service.impl;

import io.choerodon.asgard.api.service.QuartzRealJobInstanceService;
import io.choerodon.asgard.api.service.ScheduleTaskService;
import io.choerodon.asgard.domain.QuartzTasKInstance;
import io.choerodon.asgard.domain.QuartzTask;
import io.choerodon.asgard.infra.mapper.QuartzTasKInstanceMapper;
import io.choerodon.asgard.infra.mapper.QuartzTaskMapper;
import io.choerodon.asgard.infra.utils.TriggerUtils;
import io.choerodon.asgard.quartz.QuartzDefinition;
import io.choerodon.core.exception.CommonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class QuartzRealJobServiceImpl extends QuartzRealJobInstanceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuartzRealJobInstanceService.class);

    private QuartzTaskMapper taskMapper;

    private QuartzTasKInstanceMapper instanceMapper;

    private ScheduleTaskService scheduleTaskService;

    public QuartzRealJobServiceImpl(QuartzTaskMapper taskMapper,
                                    QuartzTasKInstanceMapper instanceMapper,
                                    ScheduleTaskService scheduleTaskService) {
        this.taskMapper = taskMapper;
        this.instanceMapper = instanceMapper;
        this.scheduleTaskService = scheduleTaskService;
    }

    @Override
    public void verifyUntreatedInstance(long taskId) {
        if (instanceMapper.countUnCompletedInstance(taskId) > 0) {
            scheduleTaskService.disable(taskId, null, true);
        }
    }

    @Override
    public void createInstance(long taskId) {
        QuartzTask task = taskMapper.selectByPrimaryKey(taskId);
        if (task == null) {
            LOGGER.info("task not exist when createInstance {}", taskId);
            return;
        }
        if (!QuartzDefinition.TaskStatus.ENABLE.name().equals(task.getStatus())) {
            LOGGER.info("task not enable when createInstance {}", task);
            return;
        }
        QuartzTasKInstance tasKInstance = new QuartzTasKInstance();
        tasKInstance.setTaskId(taskId);
        tasKInstance.setPlannedStartTime(new Date());
        tasKInstance.setRetriedCount(0);
        tasKInstance.setActualLastTime(instanceMapper.selectLastTime(taskId));
        tasKInstance.setStatus(QuartzDefinition.InstanceStatus.RUNNING.name());
        tasKInstance.setPlannedNextTime(TriggerUtils.getNextFireTime(task));
        if (instanceMapper.insert(tasKInstance) != 1) {
            throw new CommonException("error.quartzRealJobInstanceService.insertQuartzTaskInstance");
        }
    }

}
