package io.choerodon.asgard.api.controller.v1;

import com.github.pagehelper.PageInfo;
import io.choerodon.asgard.api.dto.ScheduleTaskInstanceDTO;
import io.choerodon.asgard.api.dto.ScheduleTaskInstanceLogDTO;
import io.choerodon.asgard.api.service.ScheduleTaskInstanceService;
import io.choerodon.base.annotation.Permission;
import io.choerodon.base.constant.PageConstant;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @GetMapping
    @ApiOperation(value = "项目层分页查询任务实例列表")
    @ResponseBody
    public ResponseEntity<PageInfo<ScheduleTaskInstanceDTO>> pagingQuery(@PathVariable("project_id") long projectId,
                                                                         @RequestParam(value = "status", required = false) String status,
                                                                         @RequestParam(name = "taskName", required = false) String taskName,
                                                                         @RequestParam(name = "exceptionMessage", required = false) String exceptionMessage,
                                                                         @RequestParam(name = "params", required = false) String params,
                                                                         @RequestParam(defaultValue = PageConstant.PAGE, required = false) final int page,
                                                                         @RequestParam(defaultValue = PageConstant.SIZE, required = false) final int size) {
        return scheduleTaskInstanceService.pageQuery(page, size, status, taskName, exceptionMessage, params, ResourceLevel.PROJECT.value(), projectId);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @GetMapping("/{taskId}")
    @ApiOperation(value = "项目层分页查询任务日志")
    @ResponseBody
    public ResponseEntity<PageInfo<ScheduleTaskInstanceLogDTO>> pagingQueryByTaskId(@PathVariable("project_id") long projectId,
                                                                                    @PathVariable long taskId,
                                                                                    @RequestParam(value = "status", required = false) String status,
                                                                                    @RequestParam(name = "serviceInstanceId", required = false) String serviceInstanceId,
                                                                                    @RequestParam(name = "params", required = false) String params,
                                                                                    @RequestParam(defaultValue = PageConstant.PAGE, required = false) final int page,
                                                                                    @RequestParam(defaultValue = PageConstant.SIZE, required = false) final int size) {
        return new ResponseEntity<>(scheduleTaskInstanceService.pagingQueryByTaskId(page, size, taskId, status, serviceInstanceId, params, ResourceLevel.PROJECT.value(), projectId), HttpStatus.OK);
    }
}
