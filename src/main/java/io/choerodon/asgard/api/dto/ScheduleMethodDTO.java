package io.choerodon.asgard.api.dto;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiModelProperty;

import io.choerodon.asgard.domain.QuartzMethod;
import io.choerodon.asgard.property.PropertyJobParam;
import io.choerodon.core.exception.CommonException;

public class ScheduleMethodDTO {

    @ApiModelProperty(value = "方法id")
    private Long id;

    @ApiModelProperty(value = "方法名")
    private String method;

    @ApiModelProperty(value = "方法编码")
    private String code;

    @ApiModelProperty(value = "方法描述")
    private String description;

    private List<PropertyJobParam> paramList;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<PropertyJobParam> getParamList() {
        return paramList;
    }

    public void setParamList(List<PropertyJobParam> paramList) {
        this.paramList = paramList;
    }

    public ScheduleMethodDTO() {
    }


    public ScheduleMethodDTO(final QuartzMethod method, final ObjectMapper objectMapper) {
        this.id = method.getId();
        this.method = method.getMethod();
        this.code = method.getCode();
        this.description = method.getDescription();
        try {
            this.paramList = objectMapper.readValue(method.getParams(), new TypeReference<List<PropertyJobParam>>() {
            });
        } catch (IOException e) {
            throw new CommonException("error.scheduleTaskDTO.jsonIOException", e);
        }
    }
}
