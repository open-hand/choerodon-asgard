package io.choerodon.asgard.api.vo;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

public class RegistrantInfo {
    @Encrypt
    @ApiModelProperty(value = "注册人Id")
    private Long id;

    @ApiModelProperty(value = "注册人登录名")
    private String loginName;

    @ApiModelProperty(value = "注册人邮箱")
    private String email;

    @ApiModelProperty(value = "注册人用户名")
    private String realName;

    @Encrypt
    @ApiModelProperty(value = "注册组织ID")
    private Long organizationId;

    @ApiModelProperty(value = "注册组织名称")
    private String organizationName;

    @Encrypt
    @ApiModelProperty(value = "adminId")
    private Long adminId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    @Override
    public String toString() {
        return "RegistrantInfo{" +
                "id=" + id +
                ", loginName='" + loginName + '\'' +
                ", email='" + email + '\'' +
                ", realName='" + realName + '\'' +
                ", organizationId=" + organizationId +
                ", organizationName='" + organizationName + '\'' +
                ", adminId=" + adminId +
                '}';
    }
}
