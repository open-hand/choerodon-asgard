package io.choerodon.asgard.api.dto;

import javax.validation.constraints.NotNull;

public class StartInstanceDTO {

    private String sagaCode;

    private String input;

    @NotNull(message = "error.startSaga.refTypeNull")
    private String refType;

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
