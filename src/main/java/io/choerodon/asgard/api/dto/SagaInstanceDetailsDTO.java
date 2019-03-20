package io.choerodon.asgard.api.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.Column;

public class SagaInstanceDetailsDTO {

    @ApiModelProperty(value = "主键ID")
    private Long id;

    @ApiModelProperty(value = "对应Saga编码")
    private String sagaCode;

    @ApiModelProperty(value = "对应Saga描述")
    private String description;

    @ApiModelProperty(value = "所属微服务")
    private String service;

    @ApiModelProperty(value = "触发层级")
    private String level;

    @ApiModelProperty(value = "关联业务类型")
    private String refType;

    @ApiModelProperty(value = "关联业务ID")
    private String refId;

    @ApiModelProperty(value = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @ApiModelProperty(value = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    @ApiModelProperty(value = "实例下完成的任务个数")
    @Column(name = "completed_count")
    private Integer completed;
    @ApiModelProperty(value = "实例下运行的任务个数")
    @Column(name = "running_count")
    private Integer running;
    @ApiModelProperty(value = "实例下回滚的任务个数")
    @Column(name = "rollback_count")
    private Integer rollback;
    @ApiModelProperty(value = "实例下失败的任务个数")
    @Column(name = "failed_count")
    private Integer failed;
    @ApiModelProperty(value = "实例下在队列里的任务个数")
    @Column(name = "queue_count")
    private Integer queue;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSagaCode() {
        return sagaCode;
    }

    public void setSagaCode(String sagaCode) {
        this.sagaCode = sagaCode;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
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

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Integer getCompleted() {
        return completed;
    }

    public void setCompleted(Integer completed) {
        this.completed = completed;
    }

    public Integer getRunning() {
        return running;
    }

    public void setRunning(Integer running) {
        this.running = running;
    }

    public Integer getRollback() {
        return rollback;
    }

    public void setRollback(Integer rollback) {
        this.rollback = rollback;
    }

    public Integer getFailed() {
        return failed;
    }

    public void setFailed(Integer failed) {
        this.failed = failed;
    }

    public Integer getQueue() {
        return queue;
    }

    public void setQueue(Integer queue) {
        this.queue = queue;
    }
}
