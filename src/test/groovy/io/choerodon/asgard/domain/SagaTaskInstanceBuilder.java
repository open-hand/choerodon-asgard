package io.choerodon.asgard.domain;

import java.util.Date;

public final class SagaTaskInstanceBuilder {
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
    private Integer timeoutSeconds;
    private String timeoutPolicy;
    private String exceptionMessage;
    private String refType;
    private String refId;
    private String concurrentLimitPolicy;
    private Integer concurrentLimitNum;
    private Date plannedStartTime;
    private Date actualStartTime;
    private Date actualEndTime;

    private SagaTaskInstanceBuilder() {
    }

    public static SagaTaskInstanceBuilder aSagaTaskInstance() {
        return new SagaTaskInstanceBuilder();
    }

    public SagaTaskInstanceBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public SagaTaskInstanceBuilder withSagaInstanceId(Long sagaInstanceId) {
        this.sagaInstanceId = sagaInstanceId;
        return this;
    }

    public SagaTaskInstanceBuilder withTaskCode(String taskCode) {
        this.taskCode = taskCode;
        return this;
    }

    public SagaTaskInstanceBuilder withSagaCode(String sagaCode) {
        this.sagaCode = sagaCode;
        return this;
    }

    public SagaTaskInstanceBuilder withInstanceLock(String instanceLock) {
        this.instanceLock = instanceLock;
        return this;
    }

    public SagaTaskInstanceBuilder withStatus(String status) {
        this.status = status;
        return this;
    }

    public SagaTaskInstanceBuilder withInputDataId(Long inputDataId) {
        this.inputDataId = inputDataId;
        return this;
    }

    public SagaTaskInstanceBuilder withOutputDataId(Long outputDataId) {
        this.outputDataId = outputDataId;
        return this;
    }

    public SagaTaskInstanceBuilder withSeq(Integer seq) {
        this.seq = seq;
        return this;
    }

    public SagaTaskInstanceBuilder withMaxRetryCount(Integer maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
        return this;
    }

    public SagaTaskInstanceBuilder withRetriedCount(Integer retriedCount) {
        this.retriedCount = retriedCount;
        return this;
    }

    public SagaTaskInstanceBuilder withTimeoutSeconds(Integer timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
        return this;
    }

    public SagaTaskInstanceBuilder withTimeoutPolicy(String timeoutPolicy) {
        this.timeoutPolicy = timeoutPolicy;
        return this;
    }

    public SagaTaskInstanceBuilder withExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
        return this;
    }

    public SagaTaskInstanceBuilder withRefType(String refType) {
        this.refType = refType;
        return this;
    }

    public SagaTaskInstanceBuilder withRefId(String refId) {
        this.refId = refId;
        return this;
    }

    public SagaTaskInstanceBuilder withConcurrentLimitPolicy(String concurrentLimitPolicy) {
        this.concurrentLimitPolicy = concurrentLimitPolicy;
        return this;
    }

    public SagaTaskInstanceBuilder withConcurrentLimitNum(Integer concurrentLimitNum) {
        this.concurrentLimitNum = concurrentLimitNum;
        return this;
    }

    public SagaTaskInstanceBuilder withPlannedStartTime(Date plannedStartTime) {
        this.plannedStartTime = plannedStartTime;
        return this;
    }

    public SagaTaskInstanceBuilder withActualStartTime(Date actualStartTime) {
        this.actualStartTime = actualStartTime;
        return this;
    }

    public SagaTaskInstanceBuilder withActualEndTime(Date actualEndTime) {
        this.actualEndTime = actualEndTime;
        return this;
    }

    public SagaTaskInstance build() {
        SagaTaskInstance sagaTaskInstance = new SagaTaskInstance();
        sagaTaskInstance.setId(id);
        sagaTaskInstance.setSagaInstanceId(sagaInstanceId);
        sagaTaskInstance.setTaskCode(taskCode);
        sagaTaskInstance.setSagaCode(sagaCode);
        sagaTaskInstance.setInstanceLock(instanceLock);
        sagaTaskInstance.setStatus(status);
        sagaTaskInstance.setInputDataId(inputDataId);
        sagaTaskInstance.setOutputDataId(outputDataId);
        sagaTaskInstance.setSeq(seq);
        sagaTaskInstance.setMaxRetryCount(maxRetryCount);
        sagaTaskInstance.setRetriedCount(retriedCount);
        sagaTaskInstance.setTimeoutSeconds(timeoutSeconds);
        sagaTaskInstance.setTimeoutPolicy(timeoutPolicy);
        sagaTaskInstance.setExceptionMessage(exceptionMessage);
        sagaTaskInstance.setRefType(refType);
        sagaTaskInstance.setRefId(refId);
        sagaTaskInstance.setConcurrentLimitPolicy(concurrentLimitPolicy);
        sagaTaskInstance.setConcurrentLimitNum(concurrentLimitNum);
        sagaTaskInstance.setPlannedStartTime(plannedStartTime);
        sagaTaskInstance.setActualStartTime(actualStartTime);
        sagaTaskInstance.setActualEndTime(actualEndTime);
        return sagaTaskInstance;
    }
}
