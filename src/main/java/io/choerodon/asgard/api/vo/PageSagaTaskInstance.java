package io.choerodon.asgard.api.vo;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import java.util.Date;

public class PageSagaTaskInstance {
    @Encrypt
    @ApiModelProperty(value = "主键ID")
    private Long id;

    @Encrypt
    @ApiModelProperty(value = "Saga实例ID")
    private Long sagaInstanceId;

    @ApiModelProperty(value = "对应SagaTask 编码")
    private String taskCode;

    @ApiModelProperty(value = "对应Saga 编码")
    private String sagaCode;

    @ApiModelProperty(value = "运行的微服务实例")
    private String instanceLock;

    @ApiModelProperty(value = "状态")
    private String status;

    @ApiModelProperty(value = "运行次序")
    private Integer seq;

    @ApiModelProperty(value = "最大重试次数")
    private Integer maxRetryCount;

    @ApiModelProperty(value = "已重试次数")
    private Integer retriedCount;

    @ApiModelProperty(value = "异常信息")
    private String exceptionMessage;

    @ApiModelProperty(value = "关联业务类型")
    private String refType;

    @ApiModelProperty(value = "关联业务ID")
    private String refId;

    @ApiModelProperty(value = "并发数")
    private Integer concurrentLimitNum;

    @ApiModelProperty(value = "并发策略")
    private String concurrentLimitPolicy;

    @ApiModelProperty(value = "输入")
    private String input;

    @ApiModelProperty(value = "输出")
    private String output;

    @ApiModelProperty(value = "创建时间")
    private String creationDate;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "所属微服务")
    private String service;

    @ApiModelProperty(value = "计划开始时间")
    private Date plannedStartTime;

    @ApiModelProperty(value = "实际开始时间")
    private Date actualStartTime;

    @ApiModelProperty(value = "实际完成时间")
    private Date actualEndTime;

    @ApiModelProperty(value = "乐观锁版本号")
    private Long objectVersionNumber;

    @ApiModelProperty(value = "saga输入Id")
    private Long inputDataId;

    @ApiModelProperty(value = "saga输出Id")
    private Long outputDataId;

    public Long getInputDataId() {
        return inputDataId;
    }

    public void setInputDataId(Long inputDataId) {
        this.inputDataId = inputDataId;
    }

    public Long getOutputDataId() {
        return outputDataId;
    }

    public void setOutputDataId(Long outputDataId) {
        this.outputDataId = outputDataId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSagaInstanceId() {
        return sagaInstanceId;
    }

    public void setSagaInstanceId(Long sagaInstanceId) {
        this.sagaInstanceId = sagaInstanceId;
    }

    public String getTaskCode() {
        return taskCode;
    }

    public void setTaskCode(String taskCode) {
        this.taskCode = taskCode;
    }

    public String getSagaCode() {
        return sagaCode;
    }

    public void setSagaCode(String sagaCode) {
        this.sagaCode = sagaCode;
    }

    public String getInstanceLock() {
        return instanceLock;
    }

    public void setInstanceLock(String instanceLock) {
        this.instanceLock = instanceLock;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getSeq() {
        return seq;
    }

    public void setSeq(Integer seq) {
        this.seq = seq;
    }

    public Integer getMaxRetryCount() {
        return maxRetryCount;
    }

    public void setMaxRetryCount(Integer maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    public Integer getRetriedCount() {
        return retriedCount;
    }

    public void setRetriedCount(Integer retriedCount) {
        this.retriedCount = retriedCount;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
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

    public Integer getConcurrentLimitNum() {
        return concurrentLimitNum;
    }

    public void setConcurrentLimitNum(Integer concurrentLimitNum) {
        this.concurrentLimitNum = concurrentLimitNum;
    }

    public String getConcurrentLimitPolicy() {
        return concurrentLimitPolicy;
    }

    public void setConcurrentLimitPolicy(String concurrentLimitPolicy) {
        this.concurrentLimitPolicy = concurrentLimitPolicy;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
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

    public Date getActualEndTime() {
        return actualEndTime;
    }

    public void setActualEndTime(Date actualEndTime) {
        this.actualEndTime = actualEndTime;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }
}
