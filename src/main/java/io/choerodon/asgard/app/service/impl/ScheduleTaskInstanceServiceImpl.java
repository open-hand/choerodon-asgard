package io.choerodon.asgard.app.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.choerodon.asgard.api.vo.PollScheduleTaskInstance;
import io.choerodon.asgard.api.vo.ScheduleTask;
import io.choerodon.asgard.api.vo.ScheduleTaskInstance;
import io.choerodon.asgard.api.vo.ScheduleTaskInstanceLog;
import io.choerodon.asgard.app.service.ScheduleTaskInstanceService;
import io.choerodon.asgard.common.UpdateStatusDTO;
import io.choerodon.asgard.infra.dto.QuartzTaskInstanceDTO;
import io.choerodon.asgard.infra.mapper.QuartzTaskInstanceMapper;
import io.choerodon.asgard.infra.utils.CommonUtils;
import io.choerodon.asgard.schedule.QuartzDefinition;
import io.choerodon.asgard.schedule.dto.PollScheduleInstanceDTO;
import io.choerodon.core.exception.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ScheduleTaskInstanceServiceImpl implements ScheduleTaskInstanceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleTaskInstanceService.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    private QuartzTaskInstanceMapper instanceMapper;

    public ScheduleTaskInstanceServiceImpl(QuartzTaskInstanceMapper instanceMapper) {
        this.instanceMapper = instanceMapper;
    }

    @Override
    public ResponseEntity<PageInfo<ScheduleTaskInstance>> pageQuery(int page, int size, String status, String taskName,
                                                                    String exceptionMessage, String params, String level, Long sourceId) {


        return new ResponseEntity<>(
                PageHelper
                        .startPage(page, size)
                        .doSelectPageInfo(
                                () -> instanceMapper.
                                        fulltextSearch(status, taskName, exceptionMessage, params, level, sourceId)), HttpStatus.OK);
    }


    @Override
    public Set<PollScheduleTaskInstance> pollBatch(PollScheduleInstanceDTO dto) {
        Set<PollScheduleTaskInstance> consumerDTOS = ConcurrentHashMap.newKeySet();
        dto.getMethods().parallelStream().forEach(t -> {
            List<PollScheduleTaskInstance> methodConsumerDTOS = instanceMapper.pollBathByMethod(t);
            methodConsumerDTOS.forEach(i -> {
                if (dto.getRunningIds().contains(i.getId())) {
                    return;
                }
                //如果策略为串行且已经有在运行的消息
                if (ScheduleTask.TriggerEventStrategy.SERIAL.name().equalsIgnoreCase(i.getExecuteStrategy())
                        && hasInstanceRunning(dto.getRunningIds(), methodConsumerDTOS, i)) {
                    return;
                }
                if (i.getInstanceLock() == null) {
                    if (instanceMapper.lockByInstanceAndUpdateStartTime(i.getId(), dto.getInstance(), i.getObjectVersionNumber(), new Date()) == 1) {
                        i.setObjectVersionNumber(i.getObjectVersionNumber() + 1);
                        i.setUserDetails(CommonUtils.readJsonAsUserDetails(objectMapper, i.getUserDetailsJson()));
                        consumerDTOS.add(i);
                    }
                } else if (i.getInstanceLock().equals(dto.getInstance())) {
                    i.setUserDetails(CommonUtils.readJsonAsUserDetails(objectMapper, i.getUserDetailsJson()));
                    consumerDTOS.add(i);
                }
            });
        });
        return consumerDTOS;
    }

    /**
     * 判断同一个task下是否有实例在运行
     */
    private boolean hasInstanceRunning(final Set<Long> runningIds,
                                       final List<PollScheduleTaskInstance> pollList,
                                       final PollScheduleTaskInstance current) {
        return pollList.stream().filter(t -> t.getTaskId().equals(current.getTaskId())).anyMatch(i -> runningIds.contains(i.getId()));
    }

    @Transactional
    @Override
    public void updateStatus(final UpdateStatusDTO statusDTO) {
        if (statusDTO.getObjectVersionNumber() == null) {
            throw new FeignException("error.scheduleTaskInstanceService.updateStatus.objectVersionNumberNull");
        }
        QuartzTaskInstanceDTO dbInstance = instanceMapper.selectByPrimaryKey(statusDTO.getId());
        if (dbInstance == null) {
            throw new FeignException("error.scheduleTaskInstanceService.updateStatus.instanceNotExist");
        }
        if (QuartzDefinition.InstanceStatus.COMPLETED.name().equals(statusDTO.getStatus())) {
            if (QuartzDefinition.InstanceStatus.FAILED.name().equals(dbInstance.getStatus())) {
                throw new FeignException("error.scheduleTaskInstanceService.updateStatus.instanceWasFailed");
            }
            dbInstance.setObjectVersionNumber(statusDTO.getObjectVersionNumber());
            dbInstance.setStatus(QuartzDefinition.InstanceStatus.COMPLETED.name());
            dbInstance.setActualStartTime(new Date());
            if (!StringUtils.isEmpty(statusDTO.getOutput())) {
                dbInstance.setExecuteResult(statusDTO.getOutput());
            }
            if (instanceMapper.updateByPrimaryKeySelective(dbInstance) != 1) {
                throw new FeignException("error.scheduleTaskInstanceService.updateCompleteStatusFailed");
            }
        } else if (QuartzDefinition.InstanceStatus.FAILED.name().equals(statusDTO.getStatus())) {
            updateFailedStatus(dbInstance, statusDTO);
        }
    }

    private void updateFailedStatus(final QuartzTaskInstanceDTO dbInstance, final UpdateStatusDTO statusDTO) {
        if (dbInstance.getRetriedCount() < dbInstance.getMaxRetryCount()) {
            dbInstance.setRetriedCount(dbInstance.getRetriedCount() + 1);
            if (instanceMapper.updateByPrimaryKeySelective(dbInstance) != 1) {
                throw new FeignException("error.scheduleTaskInstanceService.updateFailedStatusFailed");
            }
        } else {
            dbInstance.setObjectVersionNumber(statusDTO.getObjectVersionNumber());
            dbInstance.setStatus(QuartzDefinition.InstanceStatus.FAILED.name());
            dbInstance.setExceptionMessage(statusDTO.getExceptionMessage());
            if (instanceMapper.updateByPrimaryKeySelective(dbInstance) != 1) {
                throw new FeignException("error.scheduleTaskInstanceService.updateFailedStatusFailed");
            }
        }
    }

    @Override
    public void unlockByInstance(String instance) {
        if (instance != null) {
            instanceMapper.unlockByInstance(instance);
        }
    }

    @Override
    public PageInfo<ScheduleTaskInstanceLog> pagingQueryByTaskId(int page, int size, Long taskId, String status, String serviceInstanceId, String params, String level, Long sourceId) {
        return PageHelper
                .startPage(page,size)
                .doSelectPageInfo(
                        () -> instanceMapper.selectByTaskId(taskId, status, serviceInstanceId, params, level, sourceId));
    }

    @Override
    public void failed(Long id, String exceptionMsg) {
        QuartzTaskInstanceDTO quartzTaskInstance = instanceMapper.selectByPrimaryKey(id);
        if (quartzTaskInstance == null) {
            LOGGER.warn("failed schedule task instance error, quartzTaskInstance is not exist {}", id);
        } else {
            quartzTaskInstance.setStatus(QuartzDefinition.InstanceStatus.FAILED.name());
            quartzTaskInstance.setExceptionMessage(exceptionMsg);
            if (instanceMapper.updateByPrimaryKey(quartzTaskInstance) == 1) {
                LOGGER.info("failed quartz instance success: {}", quartzTaskInstance);
            } else {
                LOGGER.error("failed quartz instance error, updateStatus failed : {}", quartzTaskInstance);
            }

        }
    }
}
