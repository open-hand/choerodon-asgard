package io.choerodon.asgard.api.controller;

import io.choerodon.asgard.api.dto.PollBatchDTO;
import io.choerodon.asgard.api.dto.SagaTaskInstanceDTO;
import io.choerodon.asgard.api.dto.SagaTaskInstanceStatusDTO;
import io.choerodon.asgard.api.service.SagaTaskInstanceService;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/v1/sagas/tasks/instances")
public class SagaTaskInstanceController {

    private SagaTaskInstanceService sagaTaskInstanceService;

    public SagaTaskInstanceController(SagaTaskInstanceService sagaTaskInstanceService) {
        this.sagaTaskInstanceService = sagaTaskInstanceService;
    }

    @PostMapping("/poll/batch")
    @ApiOperation(value = "内部接口。拉取指定code的任务列表，并更新instance的值")
    public ResponseEntity<List<SagaTaskInstanceDTO>> pollBatch(@RequestBody @Valid PollBatchDTO pollBatchDTO) {
        return new ResponseEntity<>(sagaTaskInstanceService.pollBatch(pollBatchDTO), HttpStatus.OK);
    }

    @PutMapping("/{id}/status")
    @ApiOperation(value = "内部接口。更新任务的执行状态")
    public void updateStatus(@PathVariable Long id, @RequestBody @Valid SagaTaskInstanceStatusDTO statusDTO) {
        statusDTO.setId(id);
        sagaTaskInstanceService.updateStatus(statusDTO);
    }

    @ApiOperation(value = "去除该消息的服务实例锁，让其他服务实例可以拉取到该消息")
    @PutMapping("/{id}/unlock")
    public void unlockById(@PathVariable Long id) {
        sagaTaskInstanceService.unlockById(id);
    }

    @ApiOperation(value = "根据服务实例批量去除消息的服务实例锁")
    @PutMapping("/unlock_by_instance")
    public void unlockByInstance(@RequestParam("instance") String instance) {
         sagaTaskInstanceService.unlockByInstance(instance);
    }

    @ApiOperation(value = "手动重试消息")
    @PutMapping("/{id}/retry")
    public void retry(@PathVariable Long id) {
        sagaTaskInstanceService.retry(id);
    }


}
