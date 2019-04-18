package io.choerodon.asgard.api.controller.v1;

import io.choerodon.asgard.api.dto.ScheduleTaskInstanceDTO;
import io.choerodon.asgard.api.dto.ScheduleTaskInstanceLogDTO;
import io.choerodon.asgard.api.service.ScheduleTaskInstanceService;
import io.choerodon.base.annotation.Permission;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
    @CustomPageRequest
    @ResponseBody
    public ResponseEntity<Page<ScheduleTaskInstanceDTO>> pagingQuery(@PathVariable("organization_id") long orgId,
                                                                                 @RequestParam(value = "status", required = false) String status,
                                                                                 @RequestParam(name = "taskName", required = false) String taskName,
                                                                                 @RequestParam(name = "exceptionMessage", required = false) String exceptionMessage,
                                                                                 @RequestParam(name = "params", required = false) String params,
                                                                                 @ApiIgnore
                                                                                 @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest) {
        return scheduleTaskInstanceService.pageQuery(pageRequest, status, taskName, exceptionMessage, params, ResourceLevel.ORGANIZATION.value(), orgId);
    }

    @Permission(type = ResourceType.ORGANIZATION)
    @GetMapping("/{taskId}")
    @ApiOperation(value = "组织层分页查询任务日志")
    @CustomPageRequest
    @ResponseBody
    public ResponseEntity<Page<ScheduleTaskInstanceLogDTO>> pagingQueryByTaskId(@PathVariable("organization_id") long orgId,
                                                                                @PathVariable long taskId,
                                                                                @RequestParam(value = "status", required = false) String status,
                                                                                @RequestParam(name = "serviceInstanceId", required = false) String serviceInstanceId,
                                                                                @RequestParam(name = "params", required = false) String params,
                                                                                @ApiIgnore
                                                                                @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest) {
        return new ResponseEntity<>(scheduleTaskInstanceService.pagingQueryByTaskId(pageRequest, taskId, status, serviceInstanceId, params, ResourceLevel.ORGANIZATION.value(), orgId), HttpStatus.OK);
    }
}
