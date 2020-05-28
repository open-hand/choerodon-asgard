package io.choerodon.asgard.infra.feign;


import io.choerodon.asgard.api.vo.*;
import io.choerodon.asgard.infra.feign.fallback.IamFeignClientFallback;
import org.hzero.common.HZeroService;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//import io.choerodon.core.notify.NoticeSendDTO;


/**
 * @author dengyouquan
 **/
@FeignClient(value = HZeroService.Iam.NAME, path = "/choerodon/v1", fallback = IamFeignClientFallback.class)
public interface IamFeignClient {
    @GetMapping(value = "/organizations/{organization_id}")
    ResponseEntity<Organization> queryOrganization(@PathVariable(name = "organization_id") Long id);

    @GetMapping(value = "/projects/{project_id}")
    ResponseEntity<Project> queryProject(@PathVariable(name = "project_id") Long id);

//    @PostMapping(value = "/site/role_members/users")
//    ResponseEntity<PageInfo<NoticeSendDTO.User>> pagingQueryUsersByRoleIdOnSiteLevel(
//            @RequestParam(value = "role_id") Long roleId,
//            @RequestParam(value = "doPage", defaultValue = "false") boolean doPage);
//
//    @PostMapping(value = "/organizations/{organization_id}/role_members/users")
//    ResponseEntity<PageInfo<NoticeSendDTO.User>> pagingQueryUsersByRoleIdOnOrganizationLevel(
//            @RequestParam(value = "role_id") Long roleId,
//            @PathVariable(name = "organization_id") Long sourceId,
//            @RequestParam(value = "doPage", defaultValue = "false") boolean doPage);
//
//    @PostMapping(value = "/projects/{project_id}/role_members/users")
//    ResponseEntity<PageInfo<NoticeSendDTO.User>> pagingQueryUsersByRoleIdOnProjectLevel(
//            @RequestParam(value = "role_id") Long roleId,
//            @PathVariable(name = "project_id") Long sourceId,
//            @RequestParam(value = "doPage", defaultValue = "false") boolean doPage);

    @GetMapping(value = "/roles")
    ResponseEntity<Role> queryByCode(@RequestParam(value = "code") String code);

    @PostMapping("/users/ids")
    ResponseEntity<List<User>> listUsersByIds(@RequestBody Long[] ids);

    @GetMapping(value = "/users/registrant")
    ResponseEntity<RegistrantInfo> queryRegistrantAndAdminId(@RequestParam(value = "org_code") String orgCode);

    @GetMapping(value = "/organizations/{organization_id}/projects/all")
    ResponseEntity<List<ProjectVO>> listProjectsByOrgId(@PathVariable(name = "organization_id") Long organizationId);
}
