package io.choerodon.asgard.api.vo;

import io.swagger.annotations.ApiModelProperty;
import java.util.Date;

/**
 * @author tom
 * @since 2019/9/9 16:06
 */
public class SagaInstanceSearchVO {

    @ApiModelProperty(value = "对应Saga 编码")
    private String sagaCode;

    @ApiModelProperty(value = "当前Saga 实例的状态")
    private String status;

    @ApiModelProperty(value = "关联业务类型")
    private String refType;

    @ApiModelProperty(value = "关联业务ID")
    private String refId;

    @ApiModelProperty(value = "参数数组")
    private String[] params;

    public SagaInstanceSearchVO() {
    }

    public SagaInstanceSearchVO(String sagaCode, String status, String refType, String refId, String[] params) {
        this.sagaCode = sagaCode;
        this.status = status;
        this.refType = refType;
        this.refId = refId;
        this.params = params;
    }

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
