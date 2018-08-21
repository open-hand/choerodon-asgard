package io.choerodon.asgard.api.dto;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.NotEmpty;

public class SagaTaskInstanceStatusDTO {

    @ApiModelProperty(value = "主键ID/必填")
    @NotNull(message = "error.updateStatus.idNull")
    private Long id;

    @ApiModelProperty(value = "状态/必填")
    @NotEmpty(message = "error.updateStatus.statusEmpty")
    private String status;

    @ApiModelProperty(value = "输出")
    private String output;

    @ApiModelProperty(value = "异常信息")
    private String exceptionMessage;

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

    @Override
    public String toString() {
        return "SagaTaskInstanceStatusDTO{" +
                "id=" + id +
                ", status='" + status + '\'' +
                ", output='" + output + '\'' +
                ", exceptionMessage='" + exceptionMessage + '\'' +
                '}';
    }
}
