package io.choerodon.asgard.api.vo;

import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * @author dengyouquan
 */
public class User {
    @Encrypt
    private Long id;

    private String loginName;

    private String realName;

    private String email;

    private String phone;

    private Long organizationId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    //    public static List<NoticeSendDTO.User> covertNotifyUser(List<User> users) {
//        if (users == null) return Collections.emptyList();
//        return users.stream().map(userDTO -> {
//            NoticeSendDTO.User user = new NoticeSendDTO.User();
//            user.setId(userDTO.getId());
//            user.setEmail(userDTO.getEmail());
//            return user;
//        }).collect(Collectors.toList());
//    }
}
