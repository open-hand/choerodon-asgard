package io.choerodon.asgard.api.dto;

import io.choerodon.core.notify.NoticeSendDTO;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author dengyouquan
 */
public class UserDTO {

    private Long id;

    private String loginName;

    private String realName;

    private String email;

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

    public static List<NoticeSendDTO.User> covertNotifyUser(List<UserDTO> users) {
        if (users == null) return Collections.emptyList();
        return users.stream().map(userDTO -> {
            NoticeSendDTO.User user = new NoticeSendDTO.User();
            user.setId(userDTO.getId());
            user.setEmail(userDTO.getEmail());
            return user;
        }).collect(Collectors.toList());
    }
}
