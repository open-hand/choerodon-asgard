package io.choerodon.asgard.api.service.impl;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.choerodon.asgard.api.dto.*;
import io.choerodon.asgard.domain.QuartzTaskDetail;
import io.choerodon.asgard.infra.mapper.QuartzTaskMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.choerodon.asgard.api.service.ScheduleMethodService;
import io.choerodon.asgard.api.service.ScheduleTaskService;
import io.choerodon.asgard.api.service.SystemNocificationService;
import io.choerodon.asgard.domain.QuartzTask;
import io.choerodon.asgard.infra.mapper.QuartzTaskInstanceMapper;
import io.choerodon.asgard.schedule.QuartzDefinition;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

@Service
public class SystemNotificationServiceImpl implements SystemNocificationService {
    private static final Logger logger = LoggerFactory.getLogger(SystemNotificationServiceImpl.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    public static final String CONTENT = "content";

    private ScheduleMethodService scheduleMethodService;
    private ScheduleTaskService scheduleTaskService;
    private QuartzTaskInstanceMapper instanceMapper;
    private QuartzTaskMapper quartzTaskMapper;

    public static final String ORG_NOTIFICATION_CODE = "organizationNotification";
    public static final String SITE_NOTIFICATION_CODE = "systemNotification";
    public static final String ORG_NOTIFICATION_NAME = "组织公告";
    public static final String SITE_NOTIFICATION_NAME = "系统公告";


    public SystemNotificationServiceImpl(ScheduleMethodService scheduleMethodService, ScheduleTaskService scheduleTaskService, QuartzTaskInstanceMapper instanceMapper, QuartzTaskMapper quartzTaskMapper) {
        this.scheduleMethodService = scheduleMethodService;
        this.scheduleTaskService = scheduleTaskService;
        this.instanceMapper = instanceMapper;
        this.quartzTaskMapper = quartzTaskMapper;
    }

    @Override
    public SystemNotificationDTO create(ResourceLevel level, SystemNotificationCreateDTO dto, Long userId, Long sourceId) {
        //获取methodId
        String methodCode = ResourceLevel.ORGANIZATION.equals(level) ? ORG_NOTIFICATION_CODE : SITE_NOTIFICATION_CODE;
        Long methodIdByCode = scheduleMethodService.getMethodIdByCode(methodCode);
        //填充methodParams
        Map<String, Object> params = new HashMap<>();
        params.put(CONTENT, dto.getContent());
        if (ResourceLevel.ORGANIZATION.equals(level)) {
            params.put("orgId", sourceId);
        }
        //创建任务dto
        ScheduleTaskDTO createTskDTO = new ScheduleTaskDTO(
                methodIdByCode, params, ResourceLevel.ORGANIZATION.equals(level) ? ORG_NOTIFICATION_NAME : SITE_NOTIFICATION_NAME,
                ResourceLevel.ORGANIZATION.equals(level) ? ORG_NOTIFICATION_NAME : SITE_NOTIFICATION_NAME,
                dto.getStartTime() == null ? new Date() : dto.getStartTime());
        //创建任务
        QuartzTask task = scheduleTaskService.create(createTskDTO, level.value(), sourceId);
        return new SystemNotificationDTO(task.getId(), dto.getContent(), dto.getStartTime(), SystemNotificationDTO.NotificationStatus.WAITING.value());
    }

    @Override
    public SystemNotificationDTO update(SystemNotificationUpdateDTO dto, ResourceLevel level, Long sourceId) {
        QuartzTaskDetail detail = quartzTaskMapper.selectTaskById(dto.getTaskId());
        if (detail.getStartTime().compareTo(new Date()) < 1) {
            throw new CommonException("error.systemNotification.update");
        }
        //更新内容
        if (dto.getStartTime() == null) {
            updateQuartzTask(dto);
            return new SystemNotificationDTO(detail.getId(), dto.getContent(), detail.getStartTime(), SystemNotificationDTO.NotificationStatus.WAITING.value());
        }
        //需要更新时间,重新创建任务
        else {
            scheduleTaskService.delete(dto.getTaskId(), ResourceLevel.SITE.value(), 0L);
            SystemNotificationCreateDTO createDTO = new SystemNotificationCreateDTO();
            createDTO.setStartTime(dto.getStartTime());
            createDTO.setContent(dto.getContent());
            return create(level, createDTO, 0L, 0L);
        }
    }

    /**
     * 更新quartztask内容
     *
     * @param dto
     */
    private void updateQuartzTask(SystemNotificationUpdateDTO dto) {
        Map<String, Object> params = new HashMap<>();
        params.put(CONTENT, dto.getContent());
        QuartzTask task = new QuartzTask();
        task.setId(dto.getTaskId());
        task.setObjectVersionNumber(dto.getObjectVersionNumber());
        try {
            task.setExecuteParams(objectMapper.writeValueAsString(params));
        } catch (JsonProcessingException e) {
            throw new CommonException("error.json.parse", e);
        }
        if (quartzTaskMapper.updateByPrimaryKeySelective(task) != 1) {
            throw new CommonException("error.quartzTask.update");
        }
    }

    @Override
    public SystemNotificationDTO getDetailById(ResourceLevel level, Long taskId, Long sourceId) {
        QuartzTask quartzTask = scheduleTaskService.getQuartzTask(taskId, level.value(), sourceId);
        String content = "";
        try {
            Map<String, Object> o = objectMapper.readValue(quartzTask.getExecuteParams(), new TypeReference<Map<String, Object>>() {
            });
            content = (String) o.get(CONTENT);
        } catch (IOException e) {
            throw new CommonException("error.notification.content.jsonIOException", e);
        }
        SystemNotificationDTO systemNotificationDTO = new SystemNotificationDTO(taskId, content, quartzTask.getStartTime(), getNotificationStatusFromTaskId(taskId));
        systemNotificationDTO.setObjectVersionNumber(quartzTask.getObjectVersionNumber());
        return systemNotificationDTO;
    }

    private String getNotificationStatusFromTaskId(Long taskId) {
        List<ScheduleTaskInstanceLogDTO> scheduleTaskInstanceLogDTOS = instanceMapper.selectByTaskId(taskId, null, null, null, null, null);
        if (scheduleTaskInstanceLogDTOS.isEmpty()) {
            return SystemNotificationDTO.NotificationStatus.WAITING.value();
        } else {
            return modifyStatus(scheduleTaskInstanceLogDTOS.get(0).getStatus(), true);
        }
    }

    /**
     * @param originStatus 待转换的状态
     * @param isPositive   true : instanceStatus -> notificationStatus;
     *                     false : notificationStatus ->instanceStatus
     * @return
     */
    private String modifyStatus(String originStatus, Boolean isPositive) {
        if (isPositive) {
            String status = SystemNotificationDTO.NotificationStatus.WAITING.value();
            if (originStatus.equalsIgnoreCase(QuartzDefinition.InstanceStatus.COMPLETED.name())) {
                status = SystemNotificationDTO.NotificationStatus.COMPLETED.value();
            } else if (originStatus.equalsIgnoreCase(QuartzDefinition.InstanceStatus.FAILED.name())) {
                status = SystemNotificationDTO.NotificationStatus.FAILED.value();
            } else if (originStatus.equalsIgnoreCase(QuartzDefinition.InstanceStatus.RUNNING.name())) {
                status = SystemNotificationDTO.NotificationStatus.SENDING.value();
            }
            return status;
        } else {
            String status = null;
            if (originStatus.equalsIgnoreCase(SystemNotificationDTO.NotificationStatus.COMPLETED.value())) {
                status = QuartzDefinition.InstanceStatus.COMPLETED.name();
            } else if (originStatus.equalsIgnoreCase(SystemNotificationDTO.NotificationStatus.FAILED.value())) {
                status = QuartzDefinition.InstanceStatus.FAILED.name();
            } else if (originStatus.equalsIgnoreCase(SystemNotificationDTO.NotificationStatus.SENDING.value())) {
                status = QuartzDefinition.InstanceStatus.RUNNING.name();
            }
            return status;
        }
    }

    @Override
    public Page<SystemNotificationDTO> pagingAll(PageRequest pageRequest, String status, String content, String params, ResourceLevel level, long sourceId) {
        if (status != null) {
            status = modifyStatus(status, false);
        }
        Page<SystemNotificationDTO> systemNotificationDTOS = scheduleTaskService.pagingAllNotification(pageRequest, status, content, params, level, sourceId);
        systemNotificationDTOS.setContent(modifyContents(systemNotificationDTOS.getContent()));
        return systemNotificationDTOS;
    }

    List<SystemNotificationDTO> modifyContents(List<SystemNotificationDTO> list) {
        list.forEach(t -> {
            Map<String, Object> o = null;
            try {
                o = objectMapper.readValue(t.getContent(), new TypeReference<Map<String, Object>>() {
                });
            } catch (IOException e) {
                throw new CommonException("error.notification.content.jsonIOException", e);
            }
            t.setContent((String) o.get(CONTENT));
        });
        return list;
    }
}
