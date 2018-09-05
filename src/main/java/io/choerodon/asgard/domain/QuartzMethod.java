package io.choerodon.asgard.domain;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Map;

@ModifyAudit
@VersionAudit
@Table(name = "ASGARD_QUARTZ_METHOD")
public class QuartzMethod extends AuditDomain  {

    @Id
    @GeneratedValue
    private Long id;

    private String method;

    private String maxRetryCount;

    private String params;

    @Transient
    private Map<String, Object> paramMap;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getMaxRetryCount() {
        return maxRetryCount;
    }

    public void setMaxRetryCount(String maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }

    public void setParamMap(Map<String, Object> paramMap) {
        this.paramMap = paramMap;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
