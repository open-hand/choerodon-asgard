package io.choerodon.asgard.api.vo;

import io.swagger.annotations.ApiModelProperty;

/**
 * Saga实例查询VO.
 *
 * @author tom
 * @since 2019/9/9
 */
public class SagaInstanceSearchVO {

    @ApiModelProperty(value = "Saga编码")
    private String sagaCode;

    @ApiModelProperty(value = "状态")
    private String status;

    @ApiModelProperty(value = "关联业务类型")
    private String refType;

    @ApiModelProperty(value = "关联业务ID")
    private String refId;

    @ApiModelProperty(value = "模糊查询数组")
    private String[] params;


    public String getSagaCode() {
        return sagaCode;
    }

    public void setSagaCode(String sagaCode) {
        this.sagaCode = sagaCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String[] getParams() {
        return params;
    }

    public void setParams(String[] params) {
        this.params = params;
    }
}
