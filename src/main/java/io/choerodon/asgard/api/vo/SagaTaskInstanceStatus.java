package io.choerodon.asgard.api.vo;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotEmpty;

public class SagaTaskInstanceStatus {

    @ApiModelProperty(value = "主键ID/必填")
    private Long id;

    @ApiModelProperty(value = "状态/必填")
    @NotEmpty(message = "error.updateStatus.statusEmpty")
    private String status;

    @ApiModelProperty(value = "输出")
    private String output;

    @ApiModelProperty(value = "异常信息")
    private String exceptionMessage;

    @ApiModelProperty(value = "版本id")
    private Long objectVersionNumber;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public SagaTaskInstanceStatus(Long id, String status, String output, String exceptionMessage) {
        this.id = id;
        this.status = status;
        this.output = output;
        this.exceptionMessage = exceptionMessage;
    }

    public SagaTaskInstanceStatus() {
    }

    @Override
    public String toString() {
        return "SagaTaskInstanceStatus{" +
                "id=" + id +
                ", status='" + status + '\'' +
                ", output='" + output + '\'' +
                ", exceptionMessage='" + exceptionMessage + '\'' +
                ", objectVersionNumber=" + objectVersionNumber +
                '}';
    }
}
