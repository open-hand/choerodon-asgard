package io.choerodon.asgard.domain;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@ModifyAudit
@VersionAudit
@Table(name = "ASGARD_ORCH_SAGA_TASK")
public class SagaTask extends AuditDomain {

    @Id
    @GeneratedValue
    private Long id;

    private String code;

    private String description;

    private String sagaCode;

    private Integer seq;

    private Integer maxRetryCount;

    private Integer timeoutSeconds;

    private String timeoutPolicy;

    private Integer concurrentLimitNum;

    private String concurrentLimitPolicy;

    private Boolean isEnabled;

    private String service;

    private String outputSchema;

    private String outputSchemaSource;

    public SagaTask(String code) {
        this.code = code;
    }

    public SagaTask(String code, String sagaCode) {
        this.code = code;
        this.sagaCode = sagaCode;
    }

    public SagaTask() {
    }

    public SagaTask(String code,  String sagaCode, Integer seq, Boolean isEnabled, String service) {
        this.code = code;
        this.sagaCode = sagaCode;
        this.seq = seq;
        this.isEnabled = isEnabled;
        this.service = service;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSagaCode() {
        return sagaCode;
    }

    public void setSagaCode(String sagaCode) {
        this.sagaCode = sagaCode;
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

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public Boolean getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(Boolean enabled) {
        isEnabled = enabled;
    }

    public Integer getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(Integer timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public String getTimeoutPolicy() {
        return timeoutPolicy;
    }

    public void setTimeoutPolicy(String timeoutPolicy) {
        this.timeoutPolicy = timeoutPolicy;
    }

    public Integer getConcurrentLimitNum() {
        return concurrentLimitNum;
    }

    public void setConcurrentLimitNum(Integer concurrentLimitNum) {
        this.concurrentLimitNum = concurrentLimitNum;
    }


    public String getOutputSchema() {
        return outputSchema;
    }

    public void setOutputSchema(String outputSchema) {
        this.outputSchema = outputSchema;
    }

    public String getConcurrentLimitPolicy() {
        return concurrentLimitPolicy;
    }

    public void setConcurrentLimitPolicy(String concurrentLimitPolicy) {
        this.concurrentLimitPolicy = concurrentLimitPolicy;
    }

    public String getOutputSchemaSource() {
        return outputSchemaSource;
    }

    public void setOutputSchemaSource(String outputSchemaSource) {
        this.outputSchemaSource = outputSchemaSource;
    }

    @Override
    public String toString() {
        return "SagaTask{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", description='" + description + '\'' +
                ", sagaCode='" + sagaCode + '\'' +
                ", seq=" + seq +
                ", maxRetryCount=" + maxRetryCount +
                ", timeoutSeconds=" + timeoutSeconds +
                ", timeoutPolicy='" + timeoutPolicy + '\'' +
                ", concurrentLimitNum=" + concurrentLimitNum +
                ", concurrentLimitPolicy='" + concurrentLimitPolicy + '\'' +
                ", isEnabled=" + isEnabled +
                ", service='" + service + '\'' +
                ", outputSchema='" + outputSchema + '\'' +
                ", outputSchemaSource='" + outputSchemaSource + '\'' +
                '}';
    }
}
