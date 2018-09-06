package io.choerodon.asgard.api.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.choerodon.asgard.domain.QuartzMethod;
import io.choerodon.asgard.property.PropertyJobParam;
import io.choerodon.core.exception.CommonException;

import java.io.IOException;
import java.util.List;

public class ScheduleMethodDTO {

    private Long id;

    private String method;

    private List<PropertyJobParam> paramList;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public List<PropertyJobParam> getParamList() {
        return paramList;
    }

    public void setParamList(List<PropertyJobParam> paramList) {
        this.paramList = paramList;
    }

    public ScheduleMethodDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ScheduleMethodDTO(final QuartzMethod method, final ObjectMapper objectMapper) {
        this.id = method.getId();
        this.method = method.getMethod();
        try {
            this.paramList = objectMapper.readValue(method.getParams(), new TypeReference<List<PropertyJobParam>>() {});
        } catch (IOException e) {
            throw new CommonException("error.scheduleTaskDTO.jsonIOException", e);
        }
    }
}
