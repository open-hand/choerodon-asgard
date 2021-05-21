package io.choerodon.asgard.infra.feign.operator;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.choerodon.asgard.api.vo.Organization;
import io.choerodon.asgard.api.vo.ProjectDTO;
import io.choerodon.asgard.api.vo.User;
import io.choerodon.asgard.infra.feign.IamFeignClient;
import io.choerodon.core.exception.CommonException;

/**
 * Created by Sheep on 2019/7/11.
 */

@Component
public class BaseServiceClientOperator {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseServiceClientOperator.class);


    @Autowired
    private IamFeignClient iamFeignClient;

    public Organization queryTenantById(Long tenantId) {
        ResponseEntity<Organization> responseEntity = iamFeignClient.queryOrganization(tenantId);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            Organization organization = responseEntity.getBody();
            if (organization != null) {
                return organization;
            }
        }
        throw new CommonException("error.iam.query.tenant.by.id");
    }

    public List<User> getUserByIds(Long[] userIds) {
        ResponseEntity<List<User>> listUsersByIds = iamFeignClient.listUsersByIds(userIds);
        if (listUsersByIds.getStatusCode().is2xxSuccessful()) {
            List<User> users = listUsersByIds.getBody();
            if (users != null) {
                return users;
            }
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("queryUserByIds: unexpected result: {}", JSONObject.toJSONString(users));
            }
        }
        throw new CommonException("error.user.get");
    }

    public List<User> listVindicators() {
        ResponseEntity<List<User>> listVindicators = iamFeignClient.listVindicators();
        if (listVindicators.getStatusCode().is2xxSuccessful()) {
            return listVindicators.getBody();
        } else {
            throw new CommonException("error.list.vindicators");
        }
    }

    public ProjectDTO getProjectByOrgIdAndCode(Long organizationId, String code) {
        ResponseEntity<ProjectDTO> projectDTORes = iamFeignClient.getProjectByOrgIdAndCode(organizationId, code);
        if (projectDTORes.getStatusCode().is2xxSuccessful()) {
            ProjectDTO projectDTO = projectDTORes.getBody();
            if (projectDTO != null) {
                return projectDTO;
            }
        }
        throw new CommonException("error.iam.queryProject.by.code");
    }

}
