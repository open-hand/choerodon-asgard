package io.choerodon.asgard.api.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.choerodon.asgard.api.dto.QuartzTaskDTO;
import io.choerodon.asgard.api.dto.ScheduleTaskDTO;
import io.choerodon.asgard.api.service.QuartzJobService;
import io.choerodon.asgard.api.service.ScheduleTaskService;
import io.choerodon.asgard.domain.QuartzMethod;
import io.choerodon.asgard.domain.QuartzTask;
import io.choerodon.asgard.infra.mapper.QuartzMethodMapper;
import io.choerodon.asgard.infra.mapper.QuartzTaskMapper;
import io.choerodon.asgard.property.PropertyJobParam;
import io.choerodon.asgard.quartz.ParamType;
import io.choerodon.asgard.quartz.QuartzDefinition;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class ScheduleTaskServiceImpl implements ScheduleTaskService {

    private static final String TASK_NOT_EXIST = "error.scheduleTask.taskNotExist";

    private final ModelMapper modelMapper = new ModelMapper();

    private final ObjectMapper objectMapper = new ObjectMapper();

    private QuartzMethodMapper methodMapper;

    private QuartzTaskMapper taskMapper;

    private QuartzJobService quartzJobService;

    public ScheduleTaskServiceImpl(QuartzMethodMapper methodMapper,
                                   QuartzTaskMapper taskMapper,
                                   QuartzJobService quartzJobService) {
        this.methodMapper = methodMapper;
        this.taskMapper = taskMapper;
        this.quartzJobService = quartzJobService;
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
            quartzTask.setExecuteMethod(method.getMethod());
            quartzTask.setExecuteParams(objectMapper.writeValueAsString(dto.getParams()));
            validExecuteParams(dto.getParams(), method.getParams());
            if (taskMapper.insert(quartzTask) != 1) {
                throw new CommonException("error.scheduleTask.create");
            }
            QuartzTask db = taskMapper.selectByPrimaryKey(quartzTask.getId());
            quartzJobService.addJob(db);
            return db;
        } catch (IOException e) {
            throw new CommonException("error.scheduleTask.createJsonIOException", e);
        }

    }

    private void validExecuteParams(final Map<String, Object> params, final String paramDefinition) throws IOException {

        List<PropertyJobParam> paramDefinitionList = objectMapper.readValue(paramDefinition, new TypeReference<List<String>>() {
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
    }

    @Override
    public ResponseEntity<Page<QuartzTaskDTO>> pageQuery(PageRequest pageRequest, String status, String name, String description, String params) {
        List<QuartzTask> quartzTasks = taskMapper.fulltextSearch(status, name, description, params);

        // 待补充

        return new ResponseEntity<>(PageHelper.doPageAndSort(pageRequest,
                () -> ConvertHelper.convertList(quartzTasks, QuartzTaskDTO.class)), HttpStatus.OK);
    }

}
