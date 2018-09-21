package io.choerodon.asgard.api.dto;

import io.swagger.annotations.ApiModelProperty;

public class ScheduleMethodInfoDTO {
    @ApiModelProperty(value = "主键ID")
    private Long id;
    @ApiModelProperty(value = "方法编码")
    private String code;
    @ApiModelProperty(value = "所属微服务")
    private String service;
    @ApiModelProperty(value = "方法名")
    private String method;
    @ApiModelProperty(value = "描述")
    private String description;
    @ApiModelProperty(value = "在线实例数")
    private Integer onlineInstanceNum;

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

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getOnlineInstanceNum() {
        return onlineInstanceNum;
    }

    public void setOnlineInstanceNum(Integer onlineInstanceNum) {
        this.onlineInstanceNum = onlineInstanceNum;
    }

    @Override
    public String toString() {
        return "ScheduleMethodInfoDTO{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", service='" + service + '\'' +
                ", method='" + method + '\'' +
                ", description='" + description + '\'' +
                ", onlineInstanceNum=" + onlineInstanceNum +
                '}';
    }

    public ScheduleMethodInfoDTO(Long id, String code, String service, String method, String description, Integer onlineInstanceNum) {
        this.id = id;
        this.code = code;
        this.service = service;
        this.method = method;
        this.description = description;
        this.onlineInstanceNum = onlineInstanceNum;
    }
}
