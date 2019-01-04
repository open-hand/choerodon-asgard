package io.choerodon.asgard.domain;

import io.choerodon.asgard.api.dto.SagaTaskInstanceDTO;

import java.util.Date;

public final class SagaTaskInstanceDTOBuilder {
    private Long id;
    private String taskCode;
    private String sagaCode;
    private String instanceLock;
    private Integer concurrentLimitNum;
    private String input;
    private Date actualStartTime;
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

    public SagaTaskInstanceDTOBuilder withTaskCode(String taskCode) {
        this.taskCode = taskCode;
        return this;
    }

    public SagaTaskInstanceDTOBuilder withActualStartTime(Date actualStartTime) {
        this.actualStartTime = actualStartTime;
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


    public SagaTaskInstanceDTOBuilder withConcurrentLimitNum(Integer concurrentLimitNum) {
        this.concurrentLimitNum = concurrentLimitNum;
        return this;
    }


    public SagaTaskInstanceDTOBuilder withInput(String input) {
        this.input = input;
        return this;
    }

    public SagaTaskInstanceDTOBuilder withObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
        return this;
    }

    public SagaTaskInstanceDTO build() {
        SagaTaskInstanceDTO sagaTaskInstanceDTO = new SagaTaskInstanceDTO();
        sagaTaskInstanceDTO.setId(id);
        sagaTaskInstanceDTO.setTaskCode(taskCode);
        sagaTaskInstanceDTO.setSagaCode(sagaCode);
        sagaTaskInstanceDTO.setInstanceLock(instanceLock);
        sagaTaskInstanceDTO.setConcurrentLimitNum(concurrentLimitNum);
        sagaTaskInstanceDTO.setInput(input);
        sagaTaskInstanceDTO.setActualStartTime(actualStartTime);
        sagaTaskInstanceDTO.setObjectVersionNumber(objectVersionNumber);
        return sagaTaskInstanceDTO;
    }
}
