package io.choerodon.asgard.api.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.asgard.api.dto.QuartzTaskDTO;
import io.choerodon.asgard.api.dto.ScheduleTaskDTO;
import io.choerodon.asgard.api.service.QuartzJobService;
import io.choerodon.asgard.api.service.ScheduleTaskService;
import io.choerodon.asgard.domain.QuartzMethod;
import io.choerodon.asgard.domain.QuartzTasKInstance;
import io.choerodon.asgard.domain.QuartzTask;
import io.choerodon.asgard.infra.mapper.QuartzMethodMapper;
import io.choerodon.asgard.infra.mapper.QuartzTaskInstanceMapper;
import io.choerodon.asgard.infra.mapper.QuartzTaskMapper;
import io.choerodon.asgard.infra.utils.TriggerUtils;
import io.choerodon.asgard.property.PropertyJobParam;
import io.choerodon.asgard.schedule.ParamType;
import io.choerodon.asgard.schedule.QuartzDefinition;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

@Service
public class ScheduleTaskServiceImpl implements ScheduleTaskService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleTaskService.class);

    private static final String TASK_NOT_EXIST = "error.scheduleTask.taskNotExist";

    private final ModelMapper modelMapper = new ModelMapper();

    private final ObjectMapper objectMapper = new ObjectMapper();

    private QuartzMethodMapper methodMapper;

    private QuartzTaskMapper taskMapper;

    private QuartzJobService quartzJobService;

    private QuartzTaskInstanceMapper instanceMapper;

    public ScheduleTaskServiceImpl(QuartzMethodMapper methodMapper,
                                   QuartzTaskMapper taskMapper,
                                   QuartzJobService quartzJobService,
                                   QuartzTaskInstanceMapper instanceMapper) {
        this.methodMapper = methodMapper;
        this.taskMapper = taskMapper;
        this.quartzJobService = quartzJobService;
        this.instanceMapper = instanceMapper;
    }

    @Override
    @Transactional
    public QuartzTask create(final ScheduleTaskDTO dto) {
        QuartzTask quartzTask = modelMapper.map(dto, QuartzTask.class);
        QuartzMethod method = methodMapper.selectByPrimaryKey(dto.getMethodId());
        if (method == null) {
            throw new CommonException("error.scheduleTask.methodNotExist");
        }
        try {
            quartzTask.setExecuteMethod(method.getCode());
            quartzTask.setId(null);
            quartzTask.setStatus(QuartzDefinition.TaskStatus.ENABLE.name());
            quartzTask.setExecuteParams(objectMapper.writeValueAsString(dto.getParams()));
            validExecuteParams(dto.getParams(), method.getParams());
            if (taskMapper.insertSelective(quartzTask) != 1) {
                throw new CommonException("error.scheduleTask.create");
            }
            QuartzTask db = taskMapper.selectByPrimaryKey(quartzTask.getId());
            quartzJobService.addJob(db);
            LOGGER.info("create job: {}", quartzTask);
            return db;
        } catch (IOException e) {
            throw new CommonException("error.scheduleTask.createJsonIOException", e);
        }

    }

    private void validExecuteParams(final Map<String, Object> params, final String paramDefinition) throws IOException {

        List<PropertyJobParam> paramDefinitionList = objectMapper.readValue(paramDefinition, new TypeReference<List<PropertyJobParam>>() {
        });
        params.forEach((k, v) -> {
            PropertyJobParam jobParam = getPropertyJobParam(k, paramDefinitionList);
            if (jobParam != null && !validExecuteParam(v, jobParam.getType())) {
                throw new CommonException("error.scheduleTask.paramInvalidType");
            }
        });
    }

    private PropertyJobParam getPropertyJobParam(final String key, final List<PropertyJobParam> paramDefinitionList) {
        for (PropertyJobParam propertyJobParam : paramDefinitionList) {
            if (key.equals(propertyJobParam.getName())) {
                return propertyJobParam;
            }
        }
        return null;
    }

    private boolean validExecuteParam(final Object value, final String type) {
        ParamType paramType = ParamType.getParamTypeByValue(type);
        if (paramType == null) {
            throw new CommonException("error.scheduleTask.paramType");
        }
        switch (paramType) {
            case BYTE:
                return value.getClass().equals(Byte.class);
            case SHORT:
                return value.getClass().equals(Short.class);
            case CHARACTER:
                return value.getClass().equals(Character.class);
            case INTEGER:
                return value.getClass().equals(Integer.class);
            case LONG:
                return value.getClass().equals(Long.class);
            case STRING:
                return value.getClass().equals(String.class);
            case BOOLEAN:
                return value.getClass().equals(Boolean.class);
            case FLOAT:
                return value.getClass().equals(Float.class);
            case DOUBLE:
                return value.getClass().equals(Double.class);
            default:
                return false;
        }
    }

    @Transactional
    @Override
    public void enable(long id, long objectVersionNumber) {
        QuartzTask quartzTask = taskMapper.selectByPrimaryKey(id);
        if (quartzTask == null) {
            throw new CommonException(TASK_NOT_EXIST);
        }
        if (QuartzDefinition.TaskStatus.DISABLE.name().equals(quartzTask.getStatus())) {
            quartzTask.setStatus(QuartzDefinition.TaskStatus.ENABLE.name());
            quartzTask.setObjectVersionNumber(objectVersionNumber);
            if (taskMapper.updateByPrimaryKey(quartzTask) != 1) {
                throw new CommonException("error.scheduleTask.enableTaskFailed");
            }
            quartzJobService.resumeJob(id);
            LOGGER.info("enable job: {}", quartzTask);
        }
    }

    @Transactional
    @Override
    public void disable(long id, Long objectVersionNumber, boolean executeWithIn) {
        QuartzTask quartzTask = taskMapper.selectByPrimaryKey(id);
        if (quartzTask == null) {
            throw new CommonException(TASK_NOT_EXIST);
        }
        if (executeWithIn) {
            objectVersionNumber = quartzTask.getObjectVersionNumber();
        }
        if (QuartzDefinition.TaskStatus.ENABLE.name().equals(quartzTask.getStatus())) {
            quartzTask.setStatus(QuartzDefinition.TaskStatus.DISABLE.name());
            quartzTask.setObjectVersionNumber(objectVersionNumber);
            if (taskMapper.updateByPrimaryKey(quartzTask) != 1) {
                throw new CommonException("error.scheduleTask.disableTaskFailed");
            }
            quartzJobService.pauseJob(id);
            LOGGER.info("disable job: {}", quartzTask);
        }
    }

    @Transactional
    @Override
    public void delete(long id) {
        QuartzTask quartzTask = taskMapper.selectByPrimaryKey(id);
        if (quartzTask == null) {
            throw new CommonException(TASK_NOT_EXIST);
        }
        if (taskMapper.deleteByPrimaryKey(id) != 1) {
            throw new CommonException("error.scheduleTask.deleteTaskFailed");
        }
        quartzJobService.removeJob(id);
        LOGGER.info("delete job: {}", quartzTask);
    }

    @Override
    public ResponseEntity<Page<QuartzTaskDTO>> pageQuery(PageRequest pageRequest, String status, String name, String description, String params) {
        Page<QuartzTask> page = PageHelper.doPageAndSort(pageRequest,
                () -> taskMapper.fulltextSearch(status, name, description, params));
        Page<QuartzTaskDTO> pageBack = pageConvert(page);
        return new ResponseEntity<>(pageBack, HttpStatus.OK);
    }

    private Page<QuartzTaskDTO> pageConvert(Page<QuartzTask> page) {
        List<QuartzTaskDTO> quartzTaskDTOS = new ArrayList<>();
        Page<QuartzTaskDTO> pageBack = new Page<>();
        pageBack.setNumber(page.getNumber());
        pageBack.setNumberOfElements(page.getNumberOfElements());
        pageBack.setSize(page.getSize());
        pageBack.setTotalElements(page.getTotalElements());
        pageBack.setTotalPages(page.getTotalPages());
        if (page.getContent().isEmpty()) {
            return pageBack;
        } else {
            page.getContent().forEach(t -> {
                Date lastStartTime = null;
                QuartzTasKInstance lastInstance = instanceMapper.selectLastInstance(t.getId());
                if (lastInstance != null) {
                    lastStartTime = lastInstance.getActualLastTime();
                }
                quartzTaskDTOS.add(new QuartzTaskDTO(t.getId(), t.getName(), t.getDescription(), lastStartTime, TriggerUtils.getNextFireTime(t), t.getStatus(),t.getObjectVersionNumber()));
            });
            pageBack.setContent(quartzTaskDTOS);
            return pageBack;
        }
    }

    @Override
    public void finish(long id) {
        QuartzTask quartzTask = taskMapper.selectByPrimaryKey(id);
        if (quartzTask == null) {
            LOGGER.warn("finish job error, quartzTask is not exist {}", id);
        } else {
            quartzTask.setStatus(QuartzDefinition.TaskStatus.FINISHED.name());
            if (taskMapper.updateByPrimaryKey(quartzTask) == 1) {
                LOGGER.info("finish job: {}", quartzTask);
            } else {
                LOGGER.error("finish job error, updateStatus failed : {}", quartzTask);
            }

        }
    }
}
