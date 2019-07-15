package io.choerodon.asgard.api.vo;

import io.swagger.annotations.ApiModelProperty;

public class Saga {

    @ApiModelProperty(value = "主键ID")
    private Long id;

    @ApiModelProperty(value = "Saga 编码")
    private String code;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "输入数据")
    private String input;

    @ApiModelProperty(value = "所属微服务")
    private String service;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public Saga(Long id, String code, String description, String input, String service) {
        this.id = id;
        this.code = code;
        this.description = description;
        this.input = input;
        this.service = service;
    }

    public Saga() {
    }
}
