package io.choerodon.asgard.api.vo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.choerodon.core.exception.CommonException;
import io.swagger.annotations.ApiModelProperty;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ScheduleMethodParams {
    @ApiModelProperty(value = "主键ID")
    private Long id;
    @ApiModelProperty(value = "参数MAP列表格式")
    private List<Map<String, Object>> paramsList;
    @ApiModelProperty(value = "参数Json格式")
    private String paramsJson;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getParamsJson() {
        return paramsJson;
    }

    public void setParamsJson(String paramsJson) {
        this.paramsJson = paramsJson;
    }

    public List<Map<String, Object>> getParamsList() {
        return paramsList;
    }

    public void setParamsList(List<Map<String, Object>> paramsList) {
        this.paramsList = paramsList;
    }

    public ScheduleMethodParams() {
    }

    public ScheduleMethodParams(Long id, String paramsJson) {
        this.id = id;
        this.paramsJson = paramsJson;
    }

    public ScheduleMethodParams(Long id, String paramsJson, final ObjectMapper objectMapper) {
        this.id = id;
        this.paramsJson = paramsJson;
        try {
            this.paramsList = objectMapper.readValue(paramsJson, new TypeReference<List<Map<String, Object>>>() {
            });
        } catch (IOException e) {
            throw new CommonException("error.ScheduleMethodParams.jsonIOException", e);
        }
    }
}
