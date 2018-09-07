package io.choerodon.asgard.api.dto;


import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

public class ScheduleTaskInstanceDTO {

    @ApiModelProperty(value = "主键ID")
    private Long id;

    @ApiModelProperty(value = "任务执行状态")
    private String status;

    @ApiModelProperty(value = "名称")
    private String taskName;

    @ApiModelProperty(value = "失败原因")
    private String exceptionMessage;

    @ApiModelProperty(value = "计划执行时间")
    private Date plannedStartTime;

    @ApiModelProperty(value = "实际执行时间")
    private Date actualStartTime;

    @ApiModelProperty(value = "上次执行时间")
    private Date actualLastTime;

    @ApiModelProperty(value = "下次执行时间")
    private Date plannedNextTime;

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

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
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

    public Date getActualLastTime() {
        return actualLastTime;
    }

    public void setActualLastTime(Date actualLastTime) {
        this.actualLastTime = actualLastTime;
    }

    public Date getPlannedNextTime() {
        return plannedNextTime;
    }

    public void setPlannedNextTime(Date plannedNextTime) {
        this.plannedNextTime = plannedNextTime;
    }


}

