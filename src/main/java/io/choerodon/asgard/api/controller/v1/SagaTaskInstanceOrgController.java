package io.choerodon.asgard.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.asgard.api.dto.SagaTaskInstanceInfoDTO;
import io.choerodon.asgard.api.service.SagaTaskInstanceService;
import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

@RestController
@RequestMapping("/v1/sagas/organizations/{organization_id}/tasks/instances")
public class SagaTaskInstanceOrgController {

    private SagaTaskInstanceService sagaTaskInstanceService;

    public SagaTaskInstanceOrgController(SagaTaskInstanceService sagaTaskInstanceService) {
        this.sagaTaskInstanceService = sagaTaskInstanceService;
    }

    public void setSagaTaskInstanceService(SagaTaskInstanceService sagaTaskInstanceService) {
        this.sagaTaskInstanceService = sagaTaskInstanceService;
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @GetMapping
    @ApiOperation(value = "组织层分页查询SagaTask实例列表")
    @CustomPageRequest
    @ResponseBody
    public ResponseEntity<Page<SagaTaskInstanceInfoDTO>> pagingQuery(@PathVariable("organization_id") long orgId,
                                                                     @RequestParam(value = "sagaInstanceCode", required = false) String sagaInstanceCode,
                                                                     @RequestParam(name = "status", required = false) String status,
                                                                     @RequestParam(name = "taskInstanceCode", required = false) String taskInstanceCode,
                                                                     @RequestParam(name = "params", required = false) String params,
                                                                     @ApiIgnore
                                                                     @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest) {
        return sagaTaskInstanceService.pageQuery(pageRequest, sagaInstanceCode, status, taskInstanceCode, params, ResourceLevel.ORGANIZATION.value(), orgId);
    }
}
