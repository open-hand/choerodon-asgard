package io.choerodon.asgard.infra.feign;

import io.choerodon.asgard.api.dto.*;
import io.choerodon.asgard.infra.feign.fallback.IamFeignClientFallback;
import io.choerodon.core.domain.Page;
import io.choerodon.core.notify.NoticeSendDTO;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * @author dengyouquan
 **/
@FeignClient(value = "iam-service", path = "/v1", fallback = IamFeignClientFallback.class)
public interface IamFeignClient {
    @GetMapping(value = "/organizations/{organization_id}")
    ResponseEntity<OrganizationDTO> queryOrganization(@PathVariable(name = "organization_id") Long id);

    @GetMapping(value = "/projects/{project_id}")
    ResponseEntity<ProjectDTO> queryProject(@PathVariable(name = "project_id") Long id);

    @PostMapping(value = "/site/role_members/users")
    ResponseEntity<Page<NoticeSendDTO.User>> pagingQueryUsersByRoleIdOnSiteLevel(
            @RequestParam(value = "role_id") Long roleId,
            @RequestParam(value = "doPage", defaultValue = "false") boolean doPage);

    @PostMapping(value = "/organizations/{organization_id}/role_members/users")
    ResponseEntity<Page<NoticeSendDTO.User>> pagingQueryUsersByRoleIdOnOrganizationLevel(
            @RequestParam(value = "role_id") Long roleId,
            @PathVariable(name = "organization_id") Long sourceId,
            @RequestParam(value = "doPage", defaultValue = "false") boolean doPage);

    @PostMapping(value = "/projects/{project_id}/role_members/users")
    ResponseEntity<Page<NoticeSendDTO.User>> pagingQueryUsersByRoleIdOnProjectLevel(
            @RequestParam(value = "role_id") Long roleId,
            @PathVariable(name = "project_id") Long sourceId,
            @RequestParam(value = "doPage", defaultValue = "false") boolean doPage);

    @GetMapping(value = "/roles")
    ResponseEntity<RoleDTO> queryByCode(@RequestParam(value = "code") String code);

    @PostMapping("/users/ids")
    ResponseEntity<List<UserDTO>> listUsersByIds(@RequestBody Long[] ids);
}
