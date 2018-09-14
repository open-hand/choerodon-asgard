package io.choerodon.asgard.api.service.impl;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import io.choerodon.asgard.UpdateTaskInstanceStatusDTO;
import io.choerodon.asgard.api.dto.ScheduleTaskInstanceDTO;
import io.choerodon.asgard.api.dto.ScheduleTaskInstanceLogDTO;
import io.choerodon.asgard.api.service.ScheduleTaskInstanceService;
import io.choerodon.asgard.domain.QuartzTaskInstance;
import io.choerodon.asgard.infra.mapper.QuartzTaskInstanceMapper;
import io.choerodon.asgard.schedule.QuartzDefinition;
import io.choerodon.asgard.schedule.dto.ScheduleInstanceConsumerDTO;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.FeignException;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

@Service
public class ScheduleTaskInstanceServiceImpl implements ScheduleTaskInstanceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleTaskInstanceService.class);

    private QuartzTaskInstanceMapper instanceMapper;

    public ScheduleTaskInstanceServiceImpl(QuartzTaskInstanceMapper instanceMapper) {
        this.instanceMapper = instanceMapper;
    }

    @Override
    public ResponseEntity<Page<ScheduleTaskInstanceDTO>> pageQuery(PageRequest pageRequest, String status, String taskName,
                                                                   String exceptionMessage, String params) {
        return new ResponseEntity<>(PageHelper.doPageAndSort(pageRequest,
                () -> instanceMapper.fulltextSearch(status, taskName, exceptionMessage, params)), HttpStatus.OK);
    }


    @Override
    public Set<ScheduleInstanceConsumerDTO> pollBatch(final Set<String> methods, final String instance) {
        Set<ScheduleInstanceConsumerDTO> consumerDTOS = new LinkedHashSet<>();
        methods.forEach(t -> {
            List<ScheduleInstanceConsumerDTO> methodConsumerDTOS = instanceMapper.pollBathByMethod(t);
            methodConsumerDTOS.forEach(i -> {
                if (i.getInstanceLock() == null) {
                    consumerDTOS.add(i);
                } else {
                    if (instanceMapper.lockByInstanceAndUpdateStartTime(i.getId(), instance, i.getObjectVersionNumber(), new Date()) > 0) {
                        consumerDTOS.add(i);
                    }
                }
            });
        });
        return consumerDTOS;
    }

    @Override
    public void updateStatus(final UpdateTaskInstanceStatusDTO statusDTO) {
        if (statusDTO.getObjectVersionNumber() == null) {
            throw new FeignException("error.scheduleTaskInstanceService.updateStatus.objectVersionNumberNull");
        }
        QuartzTaskInstance dbInstance = instanceMapper.selectByPrimaryKey(statusDTO.getId());
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

    private void updateFailedStatus(final QuartzTaskInstance dbInstance, final UpdateTaskInstanceStatusDTO statusDTO) {
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
    public Page<ScheduleTaskInstanceLogDTO> pagingQueryByTaskId(PageRequest pageRequest, Long taskId, String status, String serviceInstanceId, String params) {
        return PageHelper.doPageAndSort(pageRequest,
                () -> instanceMapper.selectByTaskId(taskId, status, serviceInstanceId, params));
    }

    @Override
    public void failed(Long id,String exceptionMsg) {
        QuartzTaskInstance quartzTaskInstance = instanceMapper.selectByPrimaryKey(id);
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
