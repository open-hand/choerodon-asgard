package io.choerodon.asgard.api.controller.v2;

import io.choerodon.asgard.api.vo.PollScheduleTaskInstance;
import io.choerodon.asgard.app.eventhandler.SagaInstanceEventPublisher;
import io.choerodon.asgard.app.eventhandler.SagaInstanceHandler;
import io.choerodon.asgard.app.service.ScheduleTaskInstanceService;
import io.choerodon.asgard.schedule.dto.PollScheduleInstanceDTO;
import io.choerodon.core.annotation.Permission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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


    public ScheduleTaskInstanceSiteV2Controller(ScheduleTaskInstanceService scheduleTaskInstanceService,
                                                SagaInstanceHandler sagaInstanceHandler) {
        this.scheduleTaskInstanceService = scheduleTaskInstanceService;
        this.sagaInstanceHandler = sagaInstanceHandler;
    }

    public void setScheduleTaskInstanceService(ScheduleTaskInstanceService scheduleTaskInstanceService) {
        this.scheduleTaskInstanceService = scheduleTaskInstanceService;
    }

    @PostMapping("/poll")
    @Permission(permissionWithin = true)
    @ApiOperation(value = "内部接口。拉取指定method的定时任务消息列表")
    public DeferredResult<ResponseEntity<Set<PollScheduleTaskInstance>>> pollBatch(@RequestBody PollScheduleInstanceDTO dto) {
        LOGGER.info("poll ScheduleTaskInstance from {}",dto.getService());

        DeferredResult<ResponseEntity<Set<PollScheduleTaskInstance>>> deferredResult = new DeferredResult<>(60000l);
        deferredResult.onTimeout(() -> {
                    deferredResult.setResult(new ResponseEntity<>(ConcurrentHashMap.newKeySet(), HttpStatus.OK));
                    sagaInstanceHandler.removeDeferredResult(SagaInstanceEventPublisher.QUARTZ_INSTANCE_PREFIX,dto.getService(), deferredResult);
                }
        );
        Set<PollScheduleTaskInstance> pollBatch = scheduleTaskInstanceService.pollBatch(dto);
        if (!CollectionUtils.isEmpty(pollBatch)) {
            deferredResult.setResult(new ResponseEntity<>(pollBatch, HttpStatus.OK));
        } else {
            sagaInstanceHandler.addDeferredResult(SagaInstanceEventPublisher.QUARTZ_INSTANCE_PREFIX,dto.getService(), deferredResult);
        }
        return deferredResult;
    }
}
