package io.choerodon.asgard.api.vo;

import io.swagger.annotations.ApiModelProperty;

public class JsonMerge {

    @ApiModelProperty(value = "SagaTask 编码")
    private String taskCode;

    @ApiModelProperty(value = "SagaTask 输出数据")
    private String taskOutputJsonData;

    public JsonMerge() {
    }

    public JsonMerge(String taskCode, String taskOutputJsonData) {
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
        return "JsonMerge{" +
                "taskCode='" + taskCode + '\'' +
                ", taskOutputJsonData='" + taskOutputJsonData + '\'' +
                '}';
    }
}
