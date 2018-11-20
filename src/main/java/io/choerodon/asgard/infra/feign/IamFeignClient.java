package io.choerodon.asgard.infra.feign;

import io.choerodon.asgard.api.dto.OrganizationDTO;
import io.choerodon.asgard.api.dto.ProjectDTO;
import io.choerodon.asgard.infra.feign.fallback.IamFeignClientFallback;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author dengyouquan
 **/
@FeignClient(value = "iam-service", path = "/v1", fallback = IamFeignClientFallback.class)
public interface IamFeignClient {
    @GetMapping(value = "/organizations/{organization_id}")
    ResponseEntity<OrganizationDTO> queryOrganization(@PathVariable(name = "organization_id") Long id);

    @GetMapping(value = "/projects/{project_id}")
    ResponseEntity<ProjectDTO> queryProject(@PathVariable(name = "project_id") Long id);
}
