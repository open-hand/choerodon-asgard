package io.choerodon.asgard.api.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.choerodon.core.oauth.CustomUserDetails;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import java.util.Date;
import java.util.Objects;

public class SagaTaskInstance {
    @Encrypt
    @ApiModelProperty(value = "主键ID")
    private Long id;

    @ApiModelProperty(value = "对应Saga 编码")
    private String sagaCode;

    @ApiModelProperty(value = "对应SagaTask 编码")
    private String taskCode;

    @ApiModelProperty(value = "运行的微服务实例")
    private String instanceLock;

    @ApiModelProperty(value = "并发数")
    private Integer concurrentLimitNum;

    @ApiModelProperty(value = "输入")
    private String input;

    @ApiModelProperty(value = "实际开始时间")
    private Date actualStartTime;

    @ApiModelProperty(value = "创建saga的用户信息")
    private CustomUserDetails userDetails;

    @ApiModelProperty(value = "乐观锁版本号")
    private Long objectVersionNumber;

    @ApiModelProperty(value = "关联类型")
    private String refType;

    @ApiModelProperty(value = "关联id")
    private String refId;

    @JsonIgnore
    private String userDetailsJson;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTaskCode() {
        return taskCode;
    }

    public void setTaskCode(String taskCode) {
        this.taskCode = taskCode;
    }

    public String getSagaCode() {
        return sagaCode;
    }

    public void setSagaCode(String sagaCode) {
        this.sagaCode = sagaCode;
    }

    public String getInstanceLock() {
        return instanceLock;
    }

    public void setInstanceLock(String instanceLock) {
        this.instanceLock = instanceLock;
    }


    public String getRefType() {
        return refType;
    }

    public void setRefType(String refType) {
        this.refType = refType;
    }

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    public Integer getConcurrentLimitNum() {
        return concurrentLimitNum;
    }

    public void setConcurrentLimitNum(Integer concurrentLimitNum) {
        this.concurrentLimitNum = concurrentLimitNum;
    }


    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }


    public Date getActualStartTime() {
        return actualStartTime;
    }

    public void setActualStartTime(Date actualStartTime) {
        this.actualStartTime = actualStartTime;
    }


    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public CustomUserDetails getUserDetails() {
        return userDetails;
    }

    public void setUserDetails(CustomUserDetails userDetails) {
        this.userDetails = userDetails;
    }

    public String getUserDetailsJson() {
        return userDetailsJson;
    }

    public void setUserDetailsJson(String userDetailsJson) {
        this.userDetailsJson = userDetailsJson;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SagaTaskInstance that = (SagaTaskInstance) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "SagaTaskInstance{" +
                "id=" + id +
                ", objectVersionNumber=" + objectVersionNumber +
                '}';
    }
}