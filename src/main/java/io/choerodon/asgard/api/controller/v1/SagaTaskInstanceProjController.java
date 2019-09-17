package io.choerodon.asgard.api.controller.v1;

import com.github.pagehelper.PageInfo;
import io.choerodon.asgard.api.vo.SagaTaskInstanceInfo;
import io.choerodon.asgard.app.service.SagaTaskInstanceService;
import io.choerodon.base.annotation.Permission;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.base.domain.Sort;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.mybatis.annotation.SortDefault;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.swagger.annotations.ApiOperation;
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


    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_ADMINISTRATOR})
    @ApiOperation(value = "去除该消息的服务实例锁，让其他服务实例可以拉取到该消息")
    @PutMapping("/{id}/unlock")
    public void unlockById(@PathVariable("project_id") long projectId, @PathVariable long id) {
        sagaTaskInstanceService.unlockById(id);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_ADMINISTRATOR})
    @ApiOperation(value = "强制消息失败")
    @PutMapping("/{id}/failed")
    public void forceFailed(@PathVariable("project_id") long projectId, @PathVariable long id) {
        sagaTaskInstanceService.forceFailed(id);
    }


    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_ADMINISTRATOR})
    @ApiOperation(value = "根据服务实例批量去除消息的服务实例锁")
    @PutMapping("/unlock_by_instance")
    public void unlockByInstance(@PathVariable("project_id") long projectId, @RequestParam(value = "instance", required = false) String instance) {
        if (StringUtils.isEmpty(instance)) {
            throw new CommonException("error.unlockByInstance.instanceEmpty");
        }
        sagaTaskInstanceService.unlockByInstance(instance);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_ADMINISTRATOR})
    @ApiOperation(value = "手动重试消息")
    @PutMapping("/{id}/retry")
    public void retry(@PathVariable("project_id") long projectId, @PathVariable long id) {
        sagaTaskInstanceService.retry(id);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_ADMINISTRATOR})
    @GetMapping
    @ApiOperation(value = "项目层分页查询SagaTask实例列表")
    @ResponseBody
    @CustomPageRequest
    public ResponseEntity<PageInfo<SagaTaskInstanceInfo>> pagingQuery(@PathVariable("project_id") long projectId,
                                                                      @RequestParam(required = false) String taskInstanceCode,
                                                                      @RequestParam(required = false) String sagaInstanceCode,
                                                                      @RequestParam(required = false) String status,
                                                                      @RequestParam(required = false) String params,
                                                                      @ApiIgnore
                                                                      @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest) {
        return sagaTaskInstanceService.pageQuery(pageRequest, taskInstanceCode, sagaInstanceCode, status, params, ResourceLevel.PROJECT.value(), projectId);
    }
}
