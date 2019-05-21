package io.choerodon.asgard.api.controller.v1;

import com.github.pagehelper.PageInfo;
import io.choerodon.asgard.api.dto.SagaTaskInstanceDTO;
import io.choerodon.asgard.api.dto.SagaTaskInstanceInfoDTO;
import io.choerodon.asgard.api.dto.SagaTaskInstanceStatusDTO;
import io.choerodon.asgard.api.service.SagaTaskInstanceService;
import io.choerodon.asgard.saga.dto.PollSagaTaskInstanceDTO;
import io.choerodon.base.annotation.Permission;
import io.choerodon.base.constant.PageConstant;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Set;

@RestController
@RequestMapping("/v1/sagas/tasks/instances")
public class SagaTaskInstanceController {

    private SagaTaskInstanceService sagaTaskInstanceService;

    public SagaTaskInstanceController(SagaTaskInstanceService sagaTaskInstanceService) {
        this.sagaTaskInstanceService = sagaTaskInstanceService;
    }

    public void setSagaTaskInstanceService(SagaTaskInstanceService sagaTaskInstanceService) {
        this.sagaTaskInstanceService = sagaTaskInstanceService;
    }

    @PostMapping("/poll")
    @Permission(permissionWithin = true)
    @ApiOperation(value = "内部接口。拉取指定code的任务列表，并更新instance的值")
    public ResponseEntity<Set<SagaTaskInstanceDTO>> pollBatch(@RequestBody @Valid final PollSagaTaskInstanceDTO pollBatchDTO) {
        if (pollBatchDTO.getMaxPollSize() == null) {
            pollBatchDTO.setMaxPollSize(500);
        }
        return new ResponseEntity<>(sagaTaskInstanceService.pollBatch(pollBatchDTO), HttpStatus.OK);
    }

    @PutMapping("/{id}/status")
    @ApiOperation(value = "内部接口。更新任务的执行状态")
    @Permission(permissionWithin = true)
    public void updateStatus(@PathVariable Long id, @RequestBody @Valid SagaTaskInstanceStatusDTO statusDTO) {
        statusDTO.setId(id);
        sagaTaskInstanceService.updateStatus(statusDTO);
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "内部接口。查询任务状态")
    @Permission(permissionWithin = true)
    public SagaTaskInstanceDTO query(@PathVariable long id) {
        return sagaTaskInstanceService.query(id);
    }

    @Permission(type = ResourceType.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @ApiOperation(value = " ")
    @PutMapping("/{id}/failed")
    public void forceFailed(@PathVariable long id) {
        sagaTaskInstanceService.forceFailed(id);
    }

    @Permission(type = ResourceType.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @ApiOperation(value = "去除该消息的服务实例锁，让其他服务实例可以拉取到该消息")
    @PutMapping("/{id}/unlock")
    public void unlockById(@PathVariable long id) {
        sagaTaskInstanceService.unlockById(id);
    }

    @Permission(type = ResourceType.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @ApiOperation(value = "根据服务实例批量去除消息的服务实例锁")
    @PutMapping("/unlock_by_instance")
    public void unlockByInstance(@RequestParam(value = "instance", required = false) String instance) {
        if (StringUtils.isEmpty(instance)) {
            throw new CommonException("error.unlockByInstance.instanceEmpty");
        }
        sagaTaskInstanceService.unlockByInstance(instance);
    }

    @Permission(type = ResourceType.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @ApiOperation(value = "手动重试消息")
    @PutMapping("/{id}/retry")
    public void retry(@PathVariable long id) {
        sagaTaskInstanceService.retry(id);
    }


    @Permission(type = ResourceType.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @GetMapping
    @ApiOperation(value = "平台层分页查询SagaTask实例列表")
    @ResponseBody
    public ResponseEntity<PageInfo<SagaTaskInstanceInfoDTO>> pagingQuery(@RequestParam(value = "sagaInstanceCode", required = false) String sagaInstanceCode,
                                                                         @RequestParam(name = "status", required = false) String status,
                                                                         @RequestParam(name = "taskInstanceCode", required = false) String taskInstanceCode,
                                                                         @RequestParam(name = "params", required = false) String params,
                                                                         @RequestParam(defaultValue = PageConstant.PAGE, required = false) final int page,
                                                                         @RequestParam(defaultValue = PageConstant.SIZE, required = false) final int size) {
        return sagaTaskInstanceService.pageQuery(page, size, sagaInstanceCode, status, taskInstanceCode, params, null, null);
    }
}
