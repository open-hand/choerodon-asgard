package io.choerodon.asgard.api.vo;

import io.swagger.annotations.ApiModelProperty;

public class SagaTask {

    @ApiModelProperty(value = "主键ID")
    private Long id;

    @ApiModelProperty(value = "SagaTask 编码")
    private String code;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "对应Saga 编码")
    private String sagaCode;

    @ApiModelProperty(value = "执行次序")
    private Integer seq;

    @ApiModelProperty(value = "最大重试次数")
    private Integer maxRetryCount;

    @ApiModelProperty(value = "并发数")
    private Integer concurrentLimitNum;

    @ApiModelProperty(value = "并发策略")
    private String concurrentLimitPolicy;

    @ApiModelProperty(value = "是否启用")
    private Boolean isEnabled;

    @ApiModelProperty(value = "所属微服务")
    private String service;

    @ApiModelProperty(value = "输出")
    private String outputSchema;

    @ApiModelProperty(value = "输入")
    private String inputSchema;

    @ApiModelProperty(value = "输出对象类")
    private String outputSchemaSource;

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

    public Boolean getEnabled() {
        return isEnabled;
    }

    public void setEnabled(Boolean enabled) {
        isEnabled = enabled;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getOutputSchema() {
        return outputSchema;
    }

    public void setOutputSchema(String outputSchema) {
        this.outputSchema = outputSchema;
    }

    public String getInputSchema() {
        return inputSchema;
    }

    public void setInputSchema(String inputSchema) {
        this.inputSchema = inputSchema;
    }

    public String getOutputSchemaSource() {
        return outputSchemaSource;
    }

    public void setOutputSchemaSource(String outputSchemaSource) {
        this.outputSchemaSource = outputSchemaSource;
    }
}
