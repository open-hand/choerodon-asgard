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

    @ApiModelProperty(value = "实例触发层级")
    private String level;

    @ApiModelProperty(value = "实力触发的组织/项目Id")
    private Long sourceId;

    @ApiModelProperty(value = "uuid")
    private String uuid;

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

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        return "StartInstanceDTO{" +
                "sagaCode='" + sagaCode + '\'' +
                ", input='" + input + '\'' +
                ", refType='" + refType + '\'' +
                ", refId='" + refId + '\'' +
                ", level='" + level + '\'' +
                ", sourceId=" + sourceId +
                ", uuid='" + uuid + '\'' +
                '}';
    }
}
