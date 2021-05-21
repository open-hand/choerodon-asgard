package io.choerodon.asgard.infra.feign.fallback;


import io.choerodon.asgard.api.vo.*;
import io.choerodon.asgard.infra.feign.IamFeignClient;
import io.choerodon.core.exception.FeignException;
//import io.choerodon.core.notify.NoticeSendDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author dengyouquan
 **/
@Component
public class IamFeignClientFallback implements IamFeignClient {
    @Override
    public ResponseEntity<Organization> queryOrganization(Long id) {
        throw new FeignException("error.feign.iam.queryOrganization");
    }

    @Override
    public ResponseEntity<ProjectDTO> queryProject(Long id) {
        throw new FeignException("error.iam.queryProject");
    }

    @Override
    public ResponseEntity<List<UserDTO>> pagingQueryUsersByRoleIdOnSiteLevel(Long roleId) {
        throw new FeignException("error.feign.iam.queryUsersSite");
    }

    @Override
    public ResponseEntity<List<User>> listVindicators() {
        throw new FeignException("error.list.vindicators");
    }

    @Override
    public ResponseEntity<List<UserDTO>> pagingQueryUsersByRoleIdOnOrganizationLevel(Long roleId, Long sourceId) {
        throw new FeignException("error.feign.iam.queryUserOrganization");
    }

    @Override
    public ResponseEntity<List<UserDTO>> pagingQueryUsersByRoleIdOnProjectLevel(Long roleId, Long sourceId) {
        throw new FeignException("error.feign.iam.queryUsersProject");
    }

    @Override
    public ResponseEntity<Role> getSiteRoleByCode(String code) {
        throw new FeignException("error.feign.iam.getSiteRoleByCode");
    }

    @Override
    public ResponseEntity<List<Role>> listByLabelName(Long tenantId, String labelName) {
        throw new FeignException("error.feign.iam.listByLabelName");
    }

    @Override
    public ResponseEntity<List<User>> listUsersByIds(Long[] ids) {
        throw new FeignException("error.feign.iam.listUsersByIds");
    }

    @Override
    public ResponseEntity<User> queryInfo(Long id) {
        throw new FeignException("error.feign.iam.queryInfo");
    }


    @Override
    public ResponseEntity<RegistrantInfo> queryRegistrantAndAdminId(String orgCode) {
        throw new FeignException("error.feign.iam.queryRegistrantAndAdminId");
    }

    @Override
    public ResponseEntity<List<ProjectVO>> listProjectsByOrgId(Long organizationId) {
        throw new FeignException("error.feign.iam.list.all.project");
    }

    @Override
    public SysSettingVO getSetting() {
        throw new FeignException("error.feign.iam.getSetting");
    }

    @Override
    public ResponseEntity<ProjectDTO> getProjectByOrgIdAndCode(Long organizationId, String code) {
        throw new FeignException("error.iam.queryProject.by.code");
    }
}
