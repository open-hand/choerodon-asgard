package io.choerodon.asgard.api.dto;

import java.util.Date;

import io.swagger.annotations.ApiModelProperty;

public class ScheduleTaskInstanceLogDTO {
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
}
