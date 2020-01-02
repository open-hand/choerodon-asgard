package io.choerodon.asgard.api.controller.v1;

import com.github.pagehelper.PageInfo;
import io.choerodon.asgard.api.vo.SagaTaskInstanceInfo;
import io.choerodon.asgard.app.service.SagaTaskInstanceService;
import io.choerodon.core.annotation.Permission;
import io.choerodon.core.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Pageable;
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

    @Permission(type = ResourceType.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation(value = "组织层去除该消息的服务实例锁，让其他服务实例可以拉取到该消息")
    @PutMapping("/{id}/unlock")
    public void unlockById(@PathVariable("organization_id") long orgId, @PathVariable long id) {
        sagaTaskInstanceService.unlockById(id);
    }

    @Permission(type = ResourceType.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation(value = "组织层强制失败SagaTask")
    @PutMapping("/{id}/failed")
    public void forceFailed(@PathVariable("organization_id") long orgI, @PathVariable long id) {
        sagaTaskInstanceService.forceFailed(id);
    }

    @Permission(type = ResourceType.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation(value = "组织层根据服务实例批量去除消息的服务实例锁")
    @PutMapping("/unlock_by_instance")
    public void unlockByInstance(@PathVariable("organization_id") long orgId, @RequestParam(value = "instance", required = false) String instance) {
        if (StringUtils.isEmpty(instance)) {
            throw new CommonException("error.unlockByInstance.instanceEmpty");
        }
        sagaTaskInstanceService.unlockByInstance(instance);
    }

    @Permission(type = ResourceType.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation(value = "组织层手动重试SagaTask")
    @PutMapping("/{id}/retry")
    public void retry(@PathVariable("organization_id") long orgId, @PathVariable long id) {
        sagaTaskInstanceService.retry(id);
    }


    @Permission(type = ResourceType.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @GetMapping
    @ApiOperation(value = "组织层分页查询SagaTask实例列表")
    @ResponseBody
    @CustomPageRequest
    public ResponseEntity<PageInfo<SagaTaskInstanceInfo>> pagingQuery(@ApiIgnore
                                                                      @SortDefault(value = "id", direction = Sort.Direction.ASC) Pageable pageable,
                                                                      @PathVariable("organization_id") long orgId,
                                                                      @RequestParam(required = false) String taskInstanceCode,
                                                                      @RequestParam(required = false) String sagaInstanceCode,
                                                                      @RequestParam(required = false) String status,
                                                                      @RequestParam(required = false) String params) {
        return sagaTaskInstanceService.pageQuery(pageable, taskInstanceCode, sagaInstanceCode, status, params, ResourceLevel.ORGANIZATION.value(), orgId);
    }
}
