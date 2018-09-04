package io.choerodon.asgard.domain;

import io.choerodon.asgard.api.dto.SagaTaskInstanceDTO;

import java.util.Date;

public final class SagaTaskInstanceDTOBuilder {
    private Long id;
    private Long sagaInstanceId;
    private String taskCode;
    private String sagaCode;
    private String instanceLock;
    private String status;
    private Integer seq;
    private Integer maxRetryCount;
    private Integer retriedCount;
    private Integer timeoutSeconds;
    private String timeoutPolicy;
    private String exceptionMessage;
    private String refType;
    private String refId;
    private Integer concurrentLimitNum;
    private String concurrentLimitPolicy;
    private String input;
    private String output;
    private String creationDate;
    private String description;
    private String service;
    private Date plannedStartTime;
    private Date actualStartTime;
    private Date actualEndTime;
    private Long objectVersionNumber;

    private SagaTaskInstanceDTOBuilder() {
    }

    public static SagaTaskInstanceDTOBuilder aSagaTaskInstanceDTO() {
        return new SagaTaskInstanceDTOBuilder();
    }

    public SagaTaskInstanceDTOBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public SagaTaskInstanceDTOBuilder withSagaInstanceId(Long sagaInstanceId) {
        this.sagaInstanceId = sagaInstanceId;
        return this;
    }

    public SagaTaskInstanceDTOBuilder withTaskCode(String taskCode) {
        this.taskCode = taskCode;
        return this;
    }

    public SagaTaskInstanceDTOBuilder withSagaCode(String sagaCode) {
        this.sagaCode = sagaCode;
        return this;
    }

    public SagaTaskInstanceDTOBuilder withInstanceLock(String instanceLock) {
        this.instanceLock = instanceLock;
        return this;
    }

    public SagaTaskInstanceDTOBuilder withStatus(String status) {
        this.status = status;
        return this;
    }

    public SagaTaskInstanceDTOBuilder withSeq(Integer seq) {
        this.seq = seq;
        return this;
    }

    public SagaTaskInstanceDTOBuilder withMaxRetryCount(Integer maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
        return this;
    }

    public SagaTaskInstanceDTOBuilder withRetriedCount(Integer retriedCount) {
        this.retriedCount = retriedCount;
        return this;
    }

    public SagaTaskInstanceDTOBuilder withTimeoutSeconds(Integer timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
        return this;
    }

    public SagaTaskInstanceDTOBuilder withTimeoutPolicy(String timeoutPolicy) {
        this.timeoutPolicy = timeoutPolicy;
        return this;
    }

    public SagaTaskInstanceDTOBuilder withExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
        return this;
    }

    public SagaTaskInstanceDTOBuilder withRefType(String refType) {
        this.refType = refType;
        return this;
    }

    public SagaTaskInstanceDTOBuilder withRefId(String refId) {
        this.refId = refId;
        return this;
    }

    public SagaTaskInstanceDTOBuilder withConcurrentLimitNum(Integer concurrentLimitNum) {
        this.concurrentLimitNum = concurrentLimitNum;
        return this;
    }

    public SagaTaskInstanceDTOBuilder withConcurrentLimitPolicy(String concurrentLimitPolicy) {
        this.concurrentLimitPolicy = concurrentLimitPolicy;
        return this;
    }

    public SagaTaskInstanceDTOBuilder withInput(String input) {
        this.input = input;
        return this;
    }

    public SagaTaskInstanceDTOBuilder withOutput(String output) {
        this.output = output;
        return this;
    }

    public SagaTaskInstanceDTOBuilder withCreationDate(String creationDate) {
        this.creationDate = creationDate;
        return this;
    }

    public SagaTaskInstanceDTOBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public SagaTaskInstanceDTOBuilder withService(String service) {
        this.service = service;
        return this;
    }

    public SagaTaskInstanceDTOBuilder withPlannedStartTime(Date plannedStartTime) {
        this.plannedStartTime = plannedStartTime;
        return this;
    }

    public SagaTaskInstanceDTOBuilder withActualStartTime(Date actualStartTime) {
        this.actualStartTime = actualStartTime;
        return this;
    }

    public SagaTaskInstanceDTOBuilder withActualEndTime(Date actualEndTime) {
        this.actualEndTime = actualEndTime;
        return this;
    }

    public SagaTaskInstanceDTOBuilder withObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
        return this;
    }

    public SagaTaskInstanceDTO build() {
        SagaTaskInstanceDTO sagaTaskInstanceDTO = new SagaTaskInstanceDTO();
        sagaTaskInstanceDTO.setId(id);
        sagaTaskInstanceDTO.setSagaInstanceId(sagaInstanceId);
        sagaTaskInstanceDTO.setTaskCode(taskCode);
        sagaTaskInstanceDTO.setSagaCode(sagaCode);
        sagaTaskInstanceDTO.setInstanceLock(instanceLock);
        sagaTaskInstanceDTO.setStatus(status);
        sagaTaskInstanceDTO.setSeq(seq);
        sagaTaskInstanceDTO.setMaxRetryCount(maxRetryCount);
        sagaTaskInstanceDTO.setRetriedCount(retriedCount);
        sagaTaskInstanceDTO.setTimeoutSeconds(timeoutSeconds);
        sagaTaskInstanceDTO.setTimeoutPolicy(timeoutPolicy);
        sagaTaskInstanceDTO.setExceptionMessage(exceptionMessage);
        sagaTaskInstanceDTO.setRefType(refType);
        sagaTaskInstanceDTO.setRefId(refId);
        sagaTaskInstanceDTO.setConcurrentLimitNum(concurrentLimitNum);
        sagaTaskInstanceDTO.setConcurrentLimitPolicy(concurrentLimitPolicy);
        sagaTaskInstanceDTO.setInput(input);
        sagaTaskInstanceDTO.setOutput(output);
        sagaTaskInstanceDTO.setCreationDate(creationDate);
        sagaTaskInstanceDTO.setDescription(description);
        sagaTaskInstanceDTO.setService(service);
        sagaTaskInstanceDTO.setPlannedStartTime(plannedStartTime);
        sagaTaskInstanceDTO.setActualStartTime(actualStartTime);
        sagaTaskInstanceDTO.setActualEndTime(actualEndTime);
        sagaTaskInstanceDTO.setObjectVersionNumber(objectVersionNumber);
        return sagaTaskInstanceDTO;
    }
}
