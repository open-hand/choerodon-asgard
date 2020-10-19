package io.choerodon.asgard.app.service;

/**
 * @author scp
 * @date 2020/10/19
 * @description
 */
public interface SagaCheckStatusService {

    Boolean getCreateProjectSagaStatus(Long tenantId, String projectCode);
}
