package io.choerodon.asgard.api.dto;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;

public class StartInstanceDTO {

    @ApiModelProperty(value = "对应Saga 编码")
    private String sagaCode;

    @ApiModelProperty(value = "输入")
    private String input;

    @ApiModelProperty(value = "关联业务类型")
    @NotNull(message = "error.startSaga.refTypeNull")
    private String refType;

    @ApiModelProperty(value = "关联业务ID")
    @NotNull(message = "error.startSaga.refTIdNull")
    private String refId;

    public String getSagaCode() {
        return sagaCode;
    }

    public void setSagaCode(String sagaCode) {
        this.sagaCode = sagaCode;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getRefType() {
        return refType;
    }

    public void setRefType(String refType) {
        this.refType = refType;
    }

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

}
