package io.choerodon.asgard.infra.dto;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@VersionAudit
@ModifyAudit
@Table(name = "ASGARD_SAGA_TASK_INSTANCE")
public class SagaTaskInstanceDTO extends AuditDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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

    public SagaTaskInstanceDTO(String taskCode, Long outputDataId) {
        this.taskCode = taskCode;
        this.outputDataId = outputDataId;
    }

    public SagaTaskInstanceDTO() {
    }

    public SagaTaskInstanceDTO(String taskCode) {
        this.taskCode = taskCode;
    }

    public SagaTaskInstanceDTO(Long sagaInstanceId, Integer seq) {
        this.sagaInstanceId = sagaInstanceId;
        this.seq = seq;
    }

    public Long getId() {
        return id;
    }

    public SagaTaskInstanceDTO setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getSagaInstanceId() {
        return sagaInstanceId;
    }

    public SagaTaskInstanceDTO setSagaInstanceId(Long sagaInstanceId) {
        this.sagaInstanceId = sagaInstanceId;
        return this;
    }

    public String getTaskCode() {
        return taskCode;
    }

    public SagaTaskInstanceDTO setTaskCode(String taskCode) {
        this.taskCode = taskCode;
        return this;
    }

    public String getInstanceLock() {
        return instanceLock;
    }

    public SagaTaskInstanceDTO setInstanceLock(String instanceLock) {
        this.instanceLock = instanceLock;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public SagaTaskInstanceDTO setStatus(String status) {
        this.status = status;
        return this;
    }

    public Long getInputDataId() {
        return inputDataId;
    }

    public SagaTaskInstanceDTO setInputDataId(Long inputDataId) {
        this.inputDataId = inputDataId;
        return this;
    }

    public Long getOutputDataId() {
        return outputDataId;
    }

    public SagaTaskInstanceDTO setOutputDataId(Long outputDataId) {
        this.outputDataId = outputDataId;
        return this;
    }

    public Integer getSeq() {
        return seq;
    }

    public SagaTaskInstanceDTO setSeq(Integer seq) {
        this.seq = seq;
        return this;
    }

    public Integer getMaxRetryCount() {
        return maxRetryCount;
    }

    public SagaTaskInstanceDTO setMaxRetryCount(Integer maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
        return this;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public SagaTaskInstanceDTO setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
        return this;
    }

    public String getSagaCode() {
        return sagaCode;
    }

    public SagaTaskInstanceDTO setSagaCode(String sagaCode) {
        this.sagaCode = sagaCode;
        return this;
    }

    public Integer getRetriedCount() {
        return retriedCount;
    }

    public SagaTaskInstanceDTO setRetriedCount(Integer retriedCount) {
        this.retriedCount = retriedCount;
        return this;
    }

    public String getConcurrentLimitPolicy() {
        return concurrentLimitPolicy;
    }

    public SagaTaskInstanceDTO setConcurrentLimitPolicy(String concurrentLimitPolicy) {
        this.concurrentLimitPolicy = concurrentLimitPolicy;
        return this;
    }

    public Integer getConcurrentLimitNum() {
        return concurrentLimitNum;
    }

    public SagaTaskInstanceDTO setConcurrentLimitNum(Integer concurrentLimitNum) {
        this.concurrentLimitNum = concurrentLimitNum;
        return this;
    }

    public Date getPlannedStartTime() {
        return plannedStartTime;
    }

    public SagaTaskInstanceDTO setPlannedStartTime(Date plannedStartTime) {
        this.plannedStartTime = plannedStartTime;
        return this;
    }

    public Date getActualStartTime() {
        return actualStartTime;
    }

    public SagaTaskInstanceDTO setActualStartTime(Date actualStartTime) {
        this.actualStartTime = actualStartTime;
        return this;
    }

    public Date getActualEndTime() {
        return actualEndTime;
    }

    public SagaTaskInstanceDTO setActualEndTime(Date actualEndTime) {
        this.actualEndTime = actualEndTime;
        return this;
    }

    public String getService() {
        return service;
    }

    public SagaTaskInstanceDTO setService(String service) {
        this.service = service;
        return this;
    }

    @Override
    public String toString() {
        return "SagaTaskInstanceDTO{" +
                "id=" + id +
                ", status='" + status + '\'' +
                '}';
    }


}
