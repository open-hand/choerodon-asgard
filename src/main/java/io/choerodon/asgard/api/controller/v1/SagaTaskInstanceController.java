package io.choerodon.asgard.api.controller.v1;

import io.choerodon.asgard.api.vo.SagaTaskInstance;
import io.choerodon.asgard.api.vo.SagaTaskInstanceInfo;
import io.choerodon.asgard.api.vo.SagaTaskInstanceStatus;
import io.choerodon.asgard.app.service.SagaTaskInstanceService;
import io.choerodon.asgard.saga.dto.PollSagaTaskInstanceDTO;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

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
    public ResponseEntity<Set<SagaTaskInstance>> pollBatch(@RequestBody @Valid final PollSagaTaskInstanceDTO pollBatchDTO) {
        if (pollBatchDTO.getMaxPollSize() == null) {
            pollBatchDTO.setMaxPollSize(500);
        }
        return new ResponseEntity<>(sagaTaskInstanceService.pollBatch(pollBatchDTO), HttpStatus.OK);
    }

    @PutMapping("/{id}/status")
    @ApiOperation(value = "内部接口。更新任务的执行状态")
    @Permission(permissionWithin = true)
    public void updateStatus(
            @Encrypt
            @PathVariable Long id,
            @RequestBody @Valid SagaTaskInstanceStatus statusDTO) {
        statusDTO.setId(id);
        sagaTaskInstanceService.updateStatus(statusDTO);
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "内部接口。查询任务状态")
    @Permission(permissionWithin = true)
    public SagaTaskInstance query(@Encrypt @PathVariable Long id) {
        return sagaTaskInstanceService.query(id);
    }

    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @ApiOperation(value = "平台层强制失败SagaTask")
    @PutMapping("/{id}/failed")
    public void forceFailed(@Encrypt @PathVariable Long id) {
        sagaTaskInstanceService.forceFailed(id);
    }

    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @ApiOperation(value = "平台层去除该消息的服务实例锁，让其他服务实例可以拉取到该消息")
    @PutMapping("/{id}/unlock")
    public void unlockById(@Encrypt @PathVariable Long id) {
        sagaTaskInstanceService.unlockById(id);
    }

    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @ApiOperation(value = "平台层根据服务实例批量去除消息的服务实例锁")
    @PutMapping("/unlock_by_instance")
    public void unlockByInstance(@RequestParam(value = "instance", required = false) String instance) {
        if (StringUtils.isEmpty(instance)) {
            throw new CommonException("error.unlockByInstance.instanceEmpty");
        }
        sagaTaskInstanceService.unlockByInstance(instance);
    }

    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @ApiOperation(value = "平台层手动重试SagaTask")
    @PutMapping("/{id}/retry")
    public void retry(@Encrypt @PathVariable Long id) {
        sagaTaskInstanceService.retry(id);
    }


    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @GetMapping
    @ApiOperation(value = "平台层分页查询SagaTask实例列表")
    @ResponseBody
    @CustomPageRequest
    public ResponseEntity<Page<SagaTaskInstanceInfo>> pagingQuery(@RequestParam(required = false) String taskInstanceCode,
                                                                  @RequestParam(required = false) String sagaInstanceCode,
                                                                  @RequestParam(required = false) String status,
                                                                  @RequestParam(required = false) String params,
                                                                  @ApiIgnore
                                                                      @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest) {
        return sagaTaskInstanceService.pageQuery(pageRequest, taskInstanceCode, sagaInstanceCode, status, params, null, null);
    }
}
