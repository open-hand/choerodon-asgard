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
    public ResponseEntity<Project> queryProject(Long id) {
        throw new FeignException("error.iam.queryProject");
    }

//    @Override
//    public ResponseEntity<PageInfo<NoticeSendDTO.User>> pagingQueryUsersByRoleIdOnSiteLevel(Long roleId, boolean doPage) {
//        throw new FeignException("error.feign.iam.queryUsersSite");
//    }
//
//    @Override
//    public ResponseEntity<PageInfo<NoticeSendDTO.User>> pagingQueryUsersByRoleIdOnOrganizationLevel(Long roleId, Long sourceId, boolean doPage) {
//        throw new FeignException("error.feign.iam.queryUserOrganization");
//    }
//
//    @Override
//    public ResponseEntity<PageInfo<NoticeSendDTO.User>> pagingQueryUsersByRoleIdOnProjectLevel(Long roleId, Long sourceId, boolean doPage) {
//        throw new FeignException("error.feign.iam.queryUsersProject");
//    }

    @Override
    public ResponseEntity<Role> queryByCode(String code) {
        throw new FeignException("error.feign.iam.queryByCode");
    }

    @Override
    public ResponseEntity<List<User>> listUsersByIds(Long[] ids) {
        throw new FeignException("error.feign.iam.listUsersByIds");
    }


    @Override
    public ResponseEntity<RegistrantInfo> queryRegistrantAndAdminId(String orgCode) {
        throw new FeignException("error.feign.iam.queryRegistrantAndAdminId");
    }

    @Override
    public ResponseEntity<List<ProjectVO>> listProjectsByOrgId(Long organizationId) {
        throw new FeignException("error.feign.iam.list.all.project");
    }
}
