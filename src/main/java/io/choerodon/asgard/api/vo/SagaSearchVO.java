package io.choerodon.asgard.api.vo;

import io.swagger.annotations.ApiModelProperty;

/**
 * Saga查询VO.
 *
 * @author lrc
 * @since 2019/9/9
 */
public class SagaSearchVO {

    @ApiModelProperty(value = "Saga编码")
    private String code;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "所属微服务")
    private String service;

    @ApiModelProperty(value = "模糊查询数组")
    private String[] params;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String[] getParams() {
        return params;
    }

    public void setParams(String[] params) {
        this.params = params;
    }
}
