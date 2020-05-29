package io.choerodon.asgard.api.controller.v1;

import io.choerodon.asgard.api.vo.ScheduleTaskInstance;
import io.choerodon.asgard.api.vo.ScheduleTaskInstanceLog;
import io.choerodon.asgard.app.service.ScheduleTaskInstanceService;
import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/v1/schedules/projects/{project_id}/tasks/instances")
@Api("定时任务实例接口")
public class ScheduleTaskInstanceProjectController {

    private ScheduleTaskInstanceService scheduleTaskInstanceService;

    public ScheduleTaskInstanceProjectController(ScheduleTaskInstanceService scheduleTaskInstanceService) {
        this.scheduleTaskInstanceService = scheduleTaskInstanceService;
    }

    public void setScheduleTaskInstanceService(ScheduleTaskInstanceService scheduleTaskInstanceService) {
        this.scheduleTaskInstanceService = scheduleTaskInstanceService;
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @GetMapping
    @ApiOperation(value = "项目层分页查询任务实例列表")
    @ResponseBody
    @CustomPageRequest
    public ResponseEntity<Page<ScheduleTaskInstance>> pagingQuery(@PathVariable("project_id") long projectId,
                                                                  @RequestParam(value = "status", required = false) String status,
                                                                  @RequestParam(name = "taskName", required = false) String taskName,
                                                                  @RequestParam(name = "exceptionMessage", required = false) String exceptionMessage,
                                                                  @RequestParam(name = "params", required = false) String params,
                                                                  @ApiIgnore
                                                                      @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest) {
        return scheduleTaskInstanceService.pageQuery(pageRequest, status, taskName, exceptionMessage, params, ResourceLevel.PROJECT.value(), projectId);
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @GetMapping("/{taskId}")
    @ApiOperation(value = "项目层分页查询任务日志")
    @ResponseBody
    @CustomPageRequest
    public ResponseEntity<Page<ScheduleTaskInstanceLog>> pagingQueryByTaskId(@PathVariable("project_id") long projectId,
                                                                                 @PathVariable long taskId,
                                                                                 @RequestParam(value = "status", required = false) String status,
                                                                                 @RequestParam(name = "serviceInstanceId", required = false) String serviceInstanceId,
                                                                                 @RequestParam(name = "params", required = false) String params,
                                                                                 @ApiIgnore
                                                                                 @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest) {
        return new ResponseEntity<>(scheduleTaskInstanceService.pagingQueryByTaskId(pageRequest, taskId, status, serviceInstanceId, params, ResourceLevel.PROJECT.value(), projectId), HttpStatus.OK);
    }
}
