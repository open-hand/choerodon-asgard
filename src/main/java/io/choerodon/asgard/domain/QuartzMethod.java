package io.choerodon.asgard.domain;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

@ModifyAudit
@VersionAudit
@Table(name = "ASGARD_QUARTZ_METHOD")
public class QuartzMethod extends AuditDomain {

    @Id
    @GeneratedValue
    private Long id;

    private String method;

    private Integer maxRetryCount;

    private String service;

    private String params;

    private String code;

    private String description;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Integer getMaxRetryCount() {
        return maxRetryCount;
    }

    public void setMaxRetryCount(Integer maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
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

    @Override
    public String toString() {
        return "QuartzMethod{" +
                "id=" + id +
                ", method='" + method + '\'' +
                ", maxRetryCount=" + maxRetryCount +
                ", service='" + service + '\'' +
                ", params='" + params + '\'' +
                ", code='" + code + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
