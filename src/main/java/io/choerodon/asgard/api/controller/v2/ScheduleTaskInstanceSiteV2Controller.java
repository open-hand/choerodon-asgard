package io.choerodon.asgard.api.controller.v2;

import io.choerodon.asgard.api.dto.PollScheduleTaskInstanceDTO;
import io.choerodon.asgard.api.dto.SagaTaskInstanceDTO;
import io.choerodon.asgard.api.eventhandler.SagaInstanceEventPublisher;
import io.choerodon.asgard.api.eventhandler.SagaInstanceHandler;
import io.choerodon.asgard.api.service.ScheduleTaskInstanceService;
import io.choerodon.asgard.schedule.dto.PollScheduleInstanceDTO;
import io.choerodon.base.annotation.Permission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by hailuo.liu@choerodon.io on 2019/5/16.
 */
@RestController
@RequestMapping("/v1/ext/schedules/tasks/instances")
@Api("全局层定时任务实例接口")
public class ScheduleTaskInstanceSiteV2Controller {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleTaskInstanceSiteV2Controller.class);

    private ScheduleTaskInstanceService scheduleTaskInstanceService;
    private SagaInstanceHandler sagaInstanceHandler;
    private SagaInstanceEventPublisher sagaInstanceEventPublisher;


    public ScheduleTaskInstanceSiteV2Controller(ScheduleTaskInstanceService scheduleTaskInstanceService,
                                                SagaInstanceHandler sagaInstanceHandler,SagaInstanceEventPublisher sagaInstanceEventPublisher) {
        this.scheduleTaskInstanceService = scheduleTaskInstanceService;
        this.sagaInstanceHandler = sagaInstanceHandler;
        this.sagaInstanceEventPublisher = sagaInstanceEventPublisher;
    }

    public void setScheduleTaskInstanceService(ScheduleTaskInstanceService scheduleTaskInstanceService) {
        this.scheduleTaskInstanceService = scheduleTaskInstanceService;
    }

    @PostMapping("/poll")
    @Permission(permissionWithin = true)
    @ApiOperation(value = "内部接口。拉取指定method的定时任务消息列表")
    public DeferredResult<ResponseEntity<Set<PollScheduleTaskInstanceDTO>>> pollBatch(@RequestBody PollScheduleInstanceDTO dto) {
        LOGGER.info("poll from %s",dto.getService());
        DeferredResult<ResponseEntity<Set<PollScheduleTaskInstanceDTO>>> deferredResult = new DeferredResult<>(60000l);
        deferredResult.onTimeout(() -> {
                    deferredResult.setResult(new ResponseEntity<>(ConcurrentHashMap.newKeySet(), HttpStatus.OK));
                    sagaInstanceHandler.removeDeferredResult(SagaInstanceEventPublisher.QUARTZ_INSTANCE_PREFIX,dto.getService(), deferredResult);
                }
        );
        Set<PollScheduleTaskInstanceDTO> pollBatch = scheduleTaskInstanceService.pollBatch(dto);
        if (pollBatch.size() > 0) {
            deferredResult.setResult(new ResponseEntity<>(pollBatch, HttpStatus.OK));
        } else {
            sagaInstanceHandler.addDeferredResult(SagaInstanceEventPublisher.QUARTZ_INSTANCE_PREFIX,dto.getService(), deferredResult);
        }
        return deferredResult;
    }
}
