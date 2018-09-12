package io.choerodon.asgard.api.dto;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiModelProperty;

import io.choerodon.core.exception.CommonException;

public class ScheduleMethodParamsDTO {
    @ApiModelProperty(value = "主键ID")
    private Long id;
    @ApiModelProperty(value = "参数MAP列表格式")
    private List<Map<String, String>> paramsList;
    @ApiModelProperty(value = "参数Json格式")
    private String paramsJson;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Map<String, String>> getParamsList() {
        return paramsList;
    }

    public void setParamsList(List<Map<String, String>> paramsList) {
        this.paramsList = paramsList;
    }

    public String getParamsJson() {
        return paramsJson;
    }

    public void setParamsJson(String paramsJson) {
        this.paramsJson = paramsJson;
    }

    public ScheduleMethodParamsDTO() {
    }

    public ScheduleMethodParamsDTO(Long id, String paramsJson) {
        this.id = id;
        this.paramsJson = paramsJson;
    }

    public ScheduleMethodParamsDTO(Long id, String paramsJson, final ObjectMapper objectMapper) {
        this.id = id;
        this.paramsJson = paramsJson;
        try {
            this.paramsList = objectMapper.readValue(paramsJson, new TypeReference<List<Map<String, String>>>() {
            });
        } catch (IOException e) {
            throw new CommonException("error.ScheduleMethodParamsDTO.jsonIOException", e);
        }
    }
}
