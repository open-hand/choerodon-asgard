package io.choerodon.asgard.domain;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@ModifyAudit
@VersionAudit
@Table(name = "ASGARD_SAGA_TASK_INSTANCE")
public class SagaTaskInstance extends AuditDomain {

    @Id
    @GeneratedValue
    private Long id;

    private Long sagaInstanceId;

    private String taskCode;

    private String sagaCode;

    private String instanceLock;

    private String status;

    private Long inputDataId;

    private Long outputDataId;

    private Integer seq;

    private Integer maxRetryCount;

    private Integer retriedCount;

    private String exceptionMessage;

    private String concurrentLimitPolicy;

    private Integer concurrentLimitNum;

    private Date plannedStartTime;

    private Date actualStartTime;

    private Date actualEndTime;

    private String service;

    public SagaTaskInstance(String taskCode, Long outputDataId) {
        this.taskCode = taskCode;
        this.outputDataId = outputDataId;
    }

    public SagaTaskInstance() {
    }

    public SagaTaskInstance(String taskCode) {
        this.taskCode = taskCode;
    }

    public SagaTaskInstance(Long sagaInstanceId, Integer seq) {
        this.sagaInstanceId = sagaInstanceId;
        this.seq = seq;
    }

    public Long getId() {
        return id;
    }

    public SagaTaskInstance setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getSagaInstanceId() {
        return sagaInstanceId;
    }

    public SagaTaskInstance setSagaInstanceId(Long sagaInstanceId) {
        this.sagaInstanceId = sagaInstanceId;
        return this;
    }

    public String getTaskCode() {
        return taskCode;
    }

    public SagaTaskInstance setTaskCode(String taskCode) {
        this.taskCode = taskCode;
        return this;
    }

    public String getInstanceLock() {
        return instanceLock;
    }

    public SagaTaskInstance setInstanceLock(String instanceLock) {
        this.instanceLock = instanceLock;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public SagaTaskInstance setStatus(String status) {
        this.status = status;
        return this;
    }

    public Long getInputDataId() {
        return inputDataId;
    }

    public SagaTaskInstance setInputDataId(Long inputDataId) {
        this.inputDataId = inputDataId;
        return this;
    }

    public Long getOutputDataId() {
        return outputDataId;
    }

    public SagaTaskInstance setOutputDataId(Long outputDataId) {
        this.outputDataId = outputDataId;
        return this;
    }

    public Integer getSeq() {
        return seq;
    }

    public SagaTaskInstance setSeq(Integer seq) {
        this.seq = seq;
        return this;
    }

    public Integer getMaxRetryCount() {
        return maxRetryCount;
    }

    public SagaTaskInstance setMaxRetryCount(Integer maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
        return this;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public SagaTaskInstance setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
        return this;
    }

    public String getSagaCode() {
        return sagaCode;
    }

    public SagaTaskInstance setSagaCode(String sagaCode) {
        this.sagaCode = sagaCode;
        return this;
    }

    public Integer getRetriedCount() {
        return retriedCount;
    }

    public SagaTaskInstance setRetriedCount(Integer retriedCount) {
        this.retriedCount = retriedCount;
        return this;
    }

    public String getConcurrentLimitPolicy() {
        return concurrentLimitPolicy;
    }

    public SagaTaskInstance setConcurrentLimitPolicy(String concurrentLimitPolicy) {
        this.concurrentLimitPolicy = concurrentLimitPolicy;
        return this;
    }

    public Integer getConcurrentLimitNum() {
        return concurrentLimitNum;
    }

    public SagaTaskInstance setConcurrentLimitNum(Integer concurrentLimitNum) {
        this.concurrentLimitNum = concurrentLimitNum;
        return this;
    }

    public Date getPlannedStartTime() {
        return plannedStartTime;
    }

    public SagaTaskInstance setPlannedStartTime(Date plannedStartTime) {
        this.plannedStartTime = plannedStartTime;
        return this;
    }

    public Date getActualStartTime() {
        return actualStartTime;
    }

    public SagaTaskInstance setActualStartTime(Date actualStartTime) {
        this.actualStartTime = actualStartTime;
        return this;
    }

    public Date getActualEndTime() {
        return actualEndTime;
    }

    public SagaTaskInstance setActualEndTime(Date actualEndTime) {
        this.actualEndTime = actualEndTime;
        return this;
    }

    public String getService() {
        return service;
    }

    public SagaTaskInstance setService(String service) {
        this.service = service;
        return this;
    }

    @Override
    public String toString() {
        return "SagaTaskInstance{" +
                "id=" + id +
                ", status='" + status + '\'' +
                '}';
    }


}
