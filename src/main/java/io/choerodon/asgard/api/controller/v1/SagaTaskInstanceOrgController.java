package io.choerodon.asgard.api.controller.v1;

import io.choerodon.asgard.api.vo.SagaTaskInstanceInfo;
import io.choerodon.asgard.app.service.SagaTaskInstanceService;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

import io.swagger.annotations.ApiOperation;

import io.choerodon.mybatis.pagehelper.domain.PageRequest;

import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

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
    @ApiOperation(value = "组织层去除该消息的服务实例锁，让其他服务实例可以拉取到该消息")
    @PutMapping("/{id}/unlock")
    public void unlockById(
            @Encrypt
            @PathVariable("organization_id") Long orgId,
            @Encrypt
            @PathVariable Long id) {
        sagaTaskInstanceService.unlockById(id);
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation(value = "组织层强制失败SagaTask")
    @PutMapping("/{id}/failed")
    public void forceFailed(
            @Encrypt
            @PathVariable("organization_id") Long orgI,
            @Encrypt
            @PathVariable Long id) {
        sagaTaskInstanceService.forceFailed(id);
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation(value = "组织层根据服务实例批量去除消息的服务实例锁")
    @PutMapping("/unlock_by_instance")
    public void unlockByInstance(
            @Encrypt
            @PathVariable("organization_id") Long orgId,
            @RequestParam(value = "instance", required = false) String instance) {
        if (StringUtils.isEmpty(instance)) {
            throw new CommonException("error.unlockByInstance.instanceEmpty");
        }
        sagaTaskInstanceService.unlockByInstance(instance);
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation(value = "组织层手动重试SagaTask")
    @PutMapping("/{id}/retry")
    public void retry(
            @Encrypt
            @PathVariable("organization_id") Long orgId,
            @Encrypt
            @PathVariable Long id) {
        sagaTaskInstanceService.retry(id);
    }


    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @GetMapping
    @ApiOperation(value = "组织层分页查询SagaTask实例列表")
    @ResponseBody
    @CustomPageRequest
    public ResponseEntity<Page<SagaTaskInstanceInfo>> pagingQuery(
            @ApiIgnore
            @SortDefault(value = "id", direction = Sort.Direction.ASC) PageRequest pageRequest,
            @Encrypt
            @PathVariable("organization_id") Long orgId,
            @RequestParam(required = false) String taskInstanceCode,
            @RequestParam(required = false) String sagaInstanceCode,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String params) {
        return sagaTaskInstanceService.pageQuery(pageRequest, taskInstanceCode, sagaInstanceCode, status, params, ResourceLevel.ORGANIZATION.value(), orgId);
    }
}
