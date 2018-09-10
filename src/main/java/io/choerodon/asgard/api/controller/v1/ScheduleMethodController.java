package io.choerodon.asgard.api.controller.v1;

import io.choerodon.asgard.api.dto.ScheduleMethodDTO;
import io.choerodon.asgard.api.service.ScheduleMethodService;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/schedules/methods")
@Api("定时任务执行方法接口")
public class ScheduleMethodController {

    private ScheduleMethodService scheduleMethodService;

    public ScheduleMethodController(ScheduleMethodService scheduleMethodService) {
        this.scheduleMethodService = scheduleMethodService;
    }

    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @ApiOperation(value = "获取执行方法列表")
    @GetMapping
    public ResponseEntity<List<ScheduleMethodDTO>> list() {
        return new ResponseEntity<>(scheduleMethodService.list(), HttpStatus.OK);
    }

}
