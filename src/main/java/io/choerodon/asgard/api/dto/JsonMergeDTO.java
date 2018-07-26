package io.choerodon.asgard.api.dto;

public class JsonMergeDTO {

    private String taskCode;

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
