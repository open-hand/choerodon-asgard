package io.choerodon.asgard.api.dto;

public class PollCodeDTO {

    private String sagaCode;
    private String taskCode;

    public PollCodeDTO(String sagaCode, String taskCode) {
        this.sagaCode = sagaCode;
        this.taskCode = taskCode;
    }

    public PollCodeDTO() {
    }

    public String getSagaCode() {
        return sagaCode;
    }

    public void setSagaCode(String sagaCode) {
        this.sagaCode = sagaCode;
    }

    public String getTaskCode() {
        return taskCode;
    }

    public void setTaskCode(String taskCode) {
        this.taskCode = taskCode;
    }

    @Override
    public String toString() {
        return "PollCodeDTO{" +
                "sagaCode='" + sagaCode + '\'' +
                ", taskCode='" + taskCode + '\'' +
                '}';
    }
}
