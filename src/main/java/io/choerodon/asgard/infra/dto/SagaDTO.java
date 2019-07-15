package io.choerodon.asgard.infra.dto;

import io.choerodon.mybatis.entity.BaseDTO;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "ASGARD_ORCH_SAGA")
public class SagaDTO extends BaseDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    private String description;

    private String inputSchema;

    private String service;

    private String inputSchemaSource;

    public SagaDTO(String code) {
        this.code = code;
    }

    public SagaDTO() {
    }

    public SagaDTO(String code, String service, String description, String inputSchema, String inputSchemaSource) {
        this.code = code;
        this.description = description;
        this.inputSchema = inputSchema;
        this.service = service;
        this.inputSchemaSource = inputSchemaSource;
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

    public String getInputSchema() {
        return inputSchema;
    }

    public void setInputSchema(String inputSchema) {
        this.inputSchema = inputSchema;
    }


    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getInputSchemaSource() {
        return inputSchemaSource;
    }

    public void setInputSchemaSource(String inputSchemaSource) {
        this.inputSchemaSource = inputSchemaSource;
    }

    @Override
    public String toString() {
        return "Saga{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", description='" + description + '\'' +
                ", inputSchema='" + inputSchema + '\'' +
                ", service='" + service + '\'' +
                ", inputSchemaSource='" + inputSchemaSource + '\'' +
                '}';
    }
}
