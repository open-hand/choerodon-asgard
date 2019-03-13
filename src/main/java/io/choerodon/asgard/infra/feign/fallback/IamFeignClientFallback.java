package io.choerodon.asgard.infra.feign.fallback;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.choerodon.asgard.api.dto.*;
import io.choerodon.asgard.infra.feign.IamFeignClient;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.FeignException;
import io.choerodon.core.notify.NoticeSendDTO;

/**
 * @author dengyouquan
 **/
@Component
public class IamFeignClientFallback implements IamFeignClient {
    @Override
    public ResponseEntity<OrganizationDTO> queryOrganization(Long id) {
        throw new FeignException("error.feign.iam.queryOrganization");
    }

    @Override
    public ResponseEntity<ProjectDTO> queryProject(Long id) {
        throw new FeignException("error.iam.queryProject");
    }

    @Override
    public ResponseEntity<Page<NoticeSendDTO.User>> pagingQueryUsersByRoleIdOnSiteLevel(Long roleId, boolean doPage) {
        throw new FeignException("error.feign.iam.queryUsersSite");
    }

    @Override
    public ResponseEntity<Page<NoticeSendDTO.User>> pagingQueryUsersByRoleIdOnOrganizationLevel(Long roleId, Long sourceId, boolean doPage) {
        throw new FeignException("error.feign.iam.queryUserOrganization");
    }

    @Override
    public ResponseEntity<Page<NoticeSendDTO.User>> pagingQueryUsersByRoleIdOnProjectLevel(Long roleId, Long sourceId, boolean doPage) {
        throw new FeignException("error.feign.iam.queryUsersProject");
    }

    @Override
    public ResponseEntity<RoleDTO> queryByCode(String code) {
        throw new FeignException("error.feign.iam.queryByCode");
    }

    @Override
    public ResponseEntity<List<UserDTO>> listUsersByIds(Long[] ids) {
        throw new FeignException("error.feign.iam.listUsersByIds");
    }


    @Override
    public ResponseEntity<RegistrantInfoDTO> queryRegistrantAndAdminId(String orgCode) {
        throw new FeignException("error.feign.iam.queryRegistrantAndAdminId");
    }
}
