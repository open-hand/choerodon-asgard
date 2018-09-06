package io.choerodon.asgard.api.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.choerodon.asgard.api.dto.ScheduleTaskDTO;
import io.choerodon.asgard.api.service.ScheduleTaskService;
import io.choerodon.asgard.domain.QuartzMethod;
import io.choerodon.asgard.domain.QuartzTask;
import io.choerodon.asgard.infra.mapper.QuartzMethodMapper;
import io.choerodon.asgard.infra.mapper.QuartzTaskMapper;
import io.choerodon.asgard.property.PropertyJobParam;
import io.choerodon.asgard.quartz.ParamType;
import io.choerodon.core.exception.CommonException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class ScheduleTaskServiceImpl implements ScheduleTaskService {

    private final ModelMapper modelMapper = new ModelMapper();

    private final ObjectMapper objectMapper = new ObjectMapper();

    private QuartzMethodMapper methodMapper;

    private QuartzTaskMapper taskMapper;

    public ScheduleTaskServiceImpl(QuartzMethodMapper methodMapper, QuartzTaskMapper taskMapper) {
        this.methodMapper = methodMapper;
        this.taskMapper = taskMapper;
    }

    @Override
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
            return taskMapper.selectByPrimaryKey(quartzTask.getId());
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

}
