package io.choerodon.asgard.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.asgard.api.dto.SagaTaskInstanceInfoDTO;
import io.choerodon.asgard.api.service.SagaTaskInstanceService;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
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
    @ApiOperation(value = "去除该消息的服务实例锁，让其他服务实例可以拉取到该消息")
    @PutMapping("/{id}/unlock")
    public void unlockById(@PathVariable("organization_id") long orgId, @PathVariable long id) {
        sagaTaskInstanceService.unlockById(id);
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation(value = "根据服务实例批量去除消息的服务实例锁")
    @PutMapping("/unlock_by_instance")
    public void unlockByInstance(@PathVariable("organization_id") long orgId, @RequestParam(value = "instance", required = false) String instance) {
        if (StringUtils.isEmpty(instance)) {
            throw new CommonException("error.unlockByInstance.instanceEmpty");
        }
        sagaTaskInstanceService.unlockByInstance(instance);
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation(value = "手动重试消息")
    @PutMapping("/{id}/retry")
    public void retry(@PathVariable("organization_id") long orgId, @PathVariable long id) {
        sagaTaskInstanceService.retry(id);
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
