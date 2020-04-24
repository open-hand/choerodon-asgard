package io.choerodon.asgard.api.vo;

import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

public class QuartzTask {

    @ApiModelProperty(value = "主键ID")
    private Long id;

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "上次执行时间")
    private Date lastExecTime;

    @ApiModelProperty(value = "下次执行时间")
    private Date nextExecTime;

    @ApiModelProperty(value = "状态")
    private String status;

    @ApiModelProperty(value = "乐观锁版本号")
    private Long objectVersionNumber;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getLastExecTime() {
        return lastExecTime;
    }

    public void setLastExecTime(Date lastExecTime) {
        this.lastExecTime = lastExecTime;
    }

    public Date getNextExecTime() {
        return nextExecTime;
    }

    public void setNextExecTime(Date nextExecTime) {
        this.nextExecTime = nextExecTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public QuartzTask() {
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public QuartzTask(Long id, String name, String description, Date lastExecTime, Date nextExecTime, String status, Long objectVersionNumber) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.lastExecTime = lastExecTime;
        this.nextExecTime = nextExecTime;
        this.status = status;
        this.objectVersionNumber = objectVersionNumber;
    }
}

