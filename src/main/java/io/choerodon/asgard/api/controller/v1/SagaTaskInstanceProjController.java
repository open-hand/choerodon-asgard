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

import java.util.List;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/v1/sagas/projects/{project_id}/tasks/instances")
public class SagaTaskInstanceProjController {

    private SagaTaskInstanceService sagaTaskInstanceService;

    public SagaTaskInstanceProjController(SagaTaskInstanceService sagaTaskInstanceService) {
        this.sagaTaskInstanceService = sagaTaskInstanceService;
    }

    public void setSagaTaskInstanceService(SagaTaskInstanceService sagaTaskInstanceService) {
        this.sagaTaskInstanceService = sagaTaskInstanceService;
    }


    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_ADMINISTRATOR, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目层去除该消息的服务实例锁，让其他服务实例可以拉取到该消息")
    @PutMapping("/{id}/unlock")
    public void unlockById(
            @PathVariable("project_id") Long projectId,
            @Encrypt
            @PathVariable Long id) {
        sagaTaskInstanceService.unlockById(id);
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_ADMINISTRATOR, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目层强制失败SagaTask")
    @PutMapping("/{id}/failed")
    public void forceFailed(
            @PathVariable("project_id") Long projectId,
            @Encrypt
            @PathVariable Long id) {
        sagaTaskInstanceService.forceFailed(id);
    }


    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_ADMINISTRATOR, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目层根据服务实例批量去除消息的服务实例锁")
    @PutMapping("/unlock_by_instance")
    public void unlockByInstance(
            @PathVariable("project_id") Long projectId,
            @RequestParam(value = "instance", required = false) String instance) {
        if (StringUtils.isEmpty(instance)) {
            throw new CommonException("error.unlockByInstance.instanceEmpty");
        }
        sagaTaskInstanceService.unlockByInstance(instance);
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_ADMINISTRATOR, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目层手动重试SagaTask")
    @PutMapping("/{id}/retry")
    public void retry(
            @PathVariable("project_id") Long projectId,
            @Encrypt
            @PathVariable Long id) {
        sagaTaskInstanceService.retry(id);
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_ADMINISTRATOR, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "重试失败的任务集合")
    @PutMapping("/retry")
    public void retrySagaTask(
            @PathVariable("project_id") Long projectId,
            @Encrypt @RequestBody List<Long> ids) {
        sagaTaskInstanceService.retrySagaTask(projectId, ids);
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_ADMINISTRATOR, InitRoleCode.PROJECT_MEMBER})
    @GetMapping
    @ApiOperation(value = "项目层分页查询SagaTask实例列表")
    @ResponseBody
    @CustomPageRequest
    public ResponseEntity<Page<SagaTaskInstanceInfo>> pagingQuery(
            @PathVariable("project_id") Long projectId,
            @RequestParam(required = false) String taskInstanceCode,
            @RequestParam(required = false) String sagaInstanceCode,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String params,
            @ApiIgnore
            @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest) {
        return sagaTaskInstanceService.pageQuery(pageRequest, taskInstanceCode, sagaInstanceCode, status, params, ResourceLevel.PROJECT.value(), projectId);
    }
}
