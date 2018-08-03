package io.choerodon.asgard.domain;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@ModifyAudit
@VersionAudit
@Table(name = "asgard_orch_saga")
public class Saga extends AuditDomain {

    @Id
    @GeneratedValue
    private Long id;

    private String code;

    private String description;

    private String inputSchema;

    private String service;

    private String inputSchemaSource;

    public Saga(String code) {
        this.code = code;
    }

    public Saga() {
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
