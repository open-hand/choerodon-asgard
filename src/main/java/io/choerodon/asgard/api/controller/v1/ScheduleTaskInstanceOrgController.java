package io.choerodon.asgard.api.controller.v1;

import com.github.pagehelper.PageInfo;
import io.choerodon.asgard.api.vo.ScheduleTaskInstance;
import io.choerodon.asgard.api.vo.ScheduleTaskInstanceLog;
import io.choerodon.asgard.app.service.ScheduleTaskInstanceService;
import io.choerodon.core.annotation.Permission;
import io.choerodon.core.enums.ResourceType;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;


@RestController
@RequestMapping("/v1/schedules/organizations/{organization_id}/tasks/instances")
@Api("定时任务实例接口")
public class ScheduleTaskInstanceOrgController {

    private ScheduleTaskInstanceService scheduleTaskInstanceService;

    public ScheduleTaskInstanceOrgController(ScheduleTaskInstanceService scheduleTaskInstanceService) {
        this.scheduleTaskInstanceService = scheduleTaskInstanceService;
    }

    public void setScheduleTaskInstanceService(ScheduleTaskInstanceService scheduleTaskInstanceService) {
        this.scheduleTaskInstanceService = scheduleTaskInstanceService;
    }

    @Permission(type = ResourceType.ORGANIZATION)
    @GetMapping
    @ApiOperation(value = "组织层分页查询任务实例列表")
    @ResponseBody
    @CustomPageRequest
    public ResponseEntity<PageInfo<ScheduleTaskInstance>> pagingQuery(@PathVariable("organization_id") long orgId,
                                                                      @RequestParam(value = "status", required = false) String status,
                                                                      @RequestParam(name = "taskName", required = false) String taskName,
                                                                      @RequestParam(name = "exceptionMessage", required = false) String exceptionMessage,
                                                                      @RequestParam(name = "params", required = false) String params,
                                                                      @ApiIgnore
                                                                      @SortDefault(value = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return scheduleTaskInstanceService.pageQuery(pageable.getPageNumber(), pageable.getPageSize(), status, taskName, exceptionMessage, params, ResourceLevel.ORGANIZATION.value(), orgId);
    }

    @Permission(type = ResourceType.ORGANIZATION)
    @GetMapping("/{taskId}")
    @ApiOperation(value = "组织层分页查询任务日志")
    @ResponseBody
    @CustomPageRequest
    public ResponseEntity<PageInfo<ScheduleTaskInstanceLog>> pagingQueryByTaskId(@PathVariable("organization_id") long orgId,
                                                                                 @PathVariable long taskId,
                                                                                 @RequestParam(value = "status", required = false) String status,
                                                                                 @RequestParam(name = "serviceInstanceId", required = false) String serviceInstanceId,
                                                                                 @RequestParam(name = "params", required = false) String params,
                                                                                 @ApiIgnore
                                                                                 @SortDefault(value = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return new ResponseEntity<>(scheduleTaskInstanceService.pagingQueryByTaskId(pageable.getPageNumber(), pageable.getPageSize(), taskId, status, serviceInstanceId, params, ResourceLevel.ORGANIZATION.value(), orgId), HttpStatus.OK);
    }
}
