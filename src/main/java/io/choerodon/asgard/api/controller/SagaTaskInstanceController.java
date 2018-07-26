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

    /**
     * 内部接口
     * SagaTask向该接口拉取消息
     */
    @PostMapping("/poll/batch")
    @ApiOperation(value = "拉取指定code的任务列表，并更新instance的值")
    public ResponseEntity<List<SagaTaskInstanceDTO>> pollBatch(@RequestBody @Valid PollBatchDTO pollBatchDTO) {
        return new ResponseEntity<>(sagaTaskInstanceService.pollBatch(pollBatchDTO), HttpStatus.OK);
    }

    /**
     * 内部接口
     * 更新任务执行的状态
     */
    @PutMapping("/{id}/status")
    @ApiOperation(value = "更新任务的执行状态")
    public void updateStatus(@PathVariable Long id, @RequestBody @Valid SagaTaskInstanceStatusDTO statusDTO) {
        statusDTO.setId(id);
        sagaTaskInstanceService.updateStatus(statusDTO);
    }




}
