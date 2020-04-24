package io.choerodon.asgard.api.vo;

import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

public class ScheduleTaskInstanceLog {
    @ApiModelProperty(value = "主键ID")
    private Long id;

    @ApiModelProperty(value = "任务执行状态")
    private String status;


    @ApiModelProperty(value = "计划执行时间")
    private Date plannedStartTime;

    @ApiModelProperty(value = "实际执行时间")
    private Date actualStartTime;

    @ApiModelProperty(value="实例Id")
    private String serviceInstanceId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getPlannedStartTime() {
        return plannedStartTime;
    }

    public void setPlannedStartTime(Date plannedStartTime) {
        this.plannedStartTime = plannedStartTime;
    }

    public Date getActualStartTime() {
        return actualStartTime;
    }

    public void setActualStartTime(Date actualStartTime) {
        this.actualStartTime = actualStartTime;
    }

    public String getServiceInstanceId() {
        return serviceInstanceId;
    }

    public void setServiceInstanceId(String serviceInstanceId) {
        this.serviceInstanceId = serviceInstanceId;
    }
}
