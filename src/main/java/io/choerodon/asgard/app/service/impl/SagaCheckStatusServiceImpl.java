package io.choerodon.asgard.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.asgard.api.vo.ProjectDTO;
import io.choerodon.asgard.app.service.SagaCheckStatusService;
import io.choerodon.asgard.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.asgard.infra.mapper.SagaTaskInstanceMapper;
import io.choerodon.asgard.saga.SagaDefinition;

/**
 * @author scp
 * @date 2020/10/19
 * @description
 */
@Service
public class SagaCheckStatusServiceImpl implements SagaCheckStatusService {
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private SagaTaskInstanceMapper sagaTaskInstanceMapper;

    @Override
    public Boolean getCreateProjectSagaStatus(Long tenantId, String projectCode) {
        ProjectDTO projectDTO = baseServiceClientOperator.getProjectByOrgIdAndCode(tenantId, projectCode);
        String status = sagaTaskInstanceMapper.getTaskInstanceStatus(projectDTO.getId().toString(), "iam-create-project");
        if (!status.isEmpty()) {
            return status.equals(SagaDefinition.TaskInstanceStatus.COMPLETED.name());
        }
        return false;
    }
}
