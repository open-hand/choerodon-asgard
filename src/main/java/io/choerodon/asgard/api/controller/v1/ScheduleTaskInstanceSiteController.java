package io.choerodon.asgard.api.controller.v1;

import io.choerodon.asgard.api.dto.ScheduleTaskInstanceDTO;
import io.choerodon.asgard.api.dto.ScheduleTaskInstanceLogDTO;
import io.choerodon.asgard.api.service.ScheduleTaskInstanceService;
import io.choerodon.asgard.common.UpdateStatusDTO;
import io.choerodon.asgard.schedule.dto.ScheduleInstanceConsumerDTO;
import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.Set;

@RestController
@RequestMapping("/v1/schedules/tasks/instances")
@Api("全局层定时任务实例接口")
public class ScheduleTaskInstanceSiteController {

    private ScheduleTaskInstanceService scheduleTaskInstanceService;

    public ScheduleTaskInstanceSiteController(ScheduleTaskInstanceService scheduleTaskInstanceService) {
        this.scheduleTaskInstanceService = scheduleTaskInstanceService;
    }

    public void setScheduleTaskInstanceService(ScheduleTaskInstanceService scheduleTaskInstanceService) {
        this.scheduleTaskInstanceService = scheduleTaskInstanceService;
    }

    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @GetMapping
    @ApiOperation(value = "全局层分页查询任务实例列表")
    @CustomPageRequest
    @ResponseBody
    public ResponseEntity<Page<ScheduleTaskInstanceDTO>> pagingQuery(@RequestParam(value = "status", required = false) String status,
                                                                     @RequestParam(name = "taskName", required = false) String taskName,
                                                                     @RequestParam(name = "exceptionMessage", required = false) String exceptionMessage,
                                                                     @RequestParam(name = "params", required = false) String params,
                                                                     @ApiIgnore
                                                                     @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest) {
        return scheduleTaskInstanceService.pageQuery(pageRequest, status, taskName, exceptionMessage, params, ResourceLevel.SITE.value(), 0L);
    }

    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @GetMapping("/{taskId}")
    @ApiOperation(value = "全局层分页查询任务日志")
    @CustomPageRequest
    @ResponseBody
    public ResponseEntity<Page<ScheduleTaskInstanceLogDTO>> pagingQueryByTaskId(@PathVariable long taskId,
                                                                                @RequestParam(value = "status", required = false) String status,
                                                                                @RequestParam(name = "serviceInstanceId", required = false) String serviceInstanceId,
                                                                                @RequestParam(name = "params", required = false) String params,
                                                                                @ApiIgnore
                                                                                @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest) {
        return new ResponseEntity<>(scheduleTaskInstanceService.pagingQueryByTaskId(pageRequest, taskId, status, serviceInstanceId, params, ResourceLevel.SITE.value(), 0L), HttpStatus.OK);
    }

    @PostMapping("/poll/batch")
    @Permission(permissionWithin = true)
    @ApiOperation(value = "内部接口。拉取指定method的定时任务消息列表")
    public ResponseEntity<Set<ScheduleInstanceConsumerDTO>> pollBatch(@RequestBody Set<String> methods, @RequestParam String instance) {
        return new ResponseEntity<>(scheduleTaskInstanceService.pollBatch(methods, instance), HttpStatus.OK);
    }

    @PutMapping("/{id}/status")
    @ApiOperation(value = "内部接口。更新任务的执行状态")
    @Permission(permissionWithin = true)
    public void updateStatus(@PathVariable long id, @RequestBody @Valid UpdateStatusDTO statusDTO) {
        statusDTO.setId(id);
        scheduleTaskInstanceService.updateStatus(statusDTO);
    }
}
