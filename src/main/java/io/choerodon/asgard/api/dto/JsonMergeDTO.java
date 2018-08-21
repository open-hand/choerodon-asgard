package io.choerodon.asgard.api.dto;

import io.swagger.annotations.ApiModelProperty;

public class JsonMergeDTO {

    @ApiModelProperty(value = "SagaTask 编码")
    private String taskCode;

    @ApiModelProperty(value = "SagaTask 输出数据")
    private String taskOutputJsonData;

    public JsonMergeDTO() {
    }

    public JsonMergeDTO(String taskCode, String taskOutputJsonData) {
        this.taskCode = taskCode;
        this.taskOutputJsonData = taskOutputJsonData;
    }

    public String getTaskCode() {
        return taskCode;
    }

    public void setTaskCode(String taskCode) {
        this.taskCode = taskCode;
    }

    public String getTaskOutputJsonData() {
        return taskOutputJsonData;
    }

    public void setTaskOutputJsonData(String taskOutputJsonData) {
        this.taskOutputJsonData = taskOutputJsonData;
    }

    @Override
    public String toString() {
        return "JsonMergeDTO{" +
                "taskCode='" + taskCode + '\'' +
                ", taskOutputJsonData='" + taskOutputJsonData + '\'' +
                '}';
    }
}
