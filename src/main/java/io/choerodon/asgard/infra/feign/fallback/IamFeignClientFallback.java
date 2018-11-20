package io.choerodon.asgard.infra.feign.fallback;

import io.choerodon.asgard.api.dto.OrganizationDTO;
import io.choerodon.asgard.api.dto.ProjectDTO;
import io.choerodon.asgard.infra.feign.IamFeignClient;
import io.choerodon.core.exception.FeignException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * @author dengyouquan
 **/
@Component
public class IamFeignClientFallback implements IamFeignClient {
    @Override
    public ResponseEntity<OrganizationDTO> queryOrganization(Long id) {
        throw new FeignException("error.organization.query");
    }

    @Override
    public ResponseEntity<ProjectDTO> queryProject(Long id) {
        throw new FeignException("error.project.query");
    }
}
