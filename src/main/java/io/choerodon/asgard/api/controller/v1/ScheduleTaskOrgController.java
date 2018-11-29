package io.choerodon.asgard.api.controller.v1;

import java.util.List;
import javax.validation.Valid;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.asgard.api.dto.QuartzTaskDTO;
import io.choerodon.asgard.api.dto.ScheduleTaskDTO;
import io.choerodon.asgard.api.dto.ScheduleTaskDetailDTO;
import io.choerodon.asgard.api.service.ScheduleTaskService;
import io.choerodon.asgard.api.validator.ScheduleTaskValidator;
import io.choerodon.asgard.domain.QuartzTask;
import io.choerodon.asgard.infra.utils.TriggerUtils;
import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

@RestController
@RequestMapping("/v1/schedules/organizations/{organization_id}/tasks")
@Api("组织层定时任务定义接口")
public class ScheduleTaskOrgController {

    private ScheduleTaskService scheduleTaskService;

    public ScheduleTaskOrgController(ScheduleTaskService scheduleTaskService) {
        this.scheduleTaskService = scheduleTaskService;
    }

    public void setScheduleTaskService(ScheduleTaskService scheduleTaskService) {
        this.scheduleTaskService = scheduleTaskService;
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "组织层创建定时任务")
    @PostMapping
    public ResponseEntity<QuartzTask> create(@PathVariable("organization_id") long orgId,
                                             @RequestBody @Valid ScheduleTaskDTO dto) {
        ScheduleTaskValidator.validatorCreate(dto);
        return new ResponseEntity<>(scheduleTaskService.create(dto, ResourceLevel.ORGANIZATION.value(), orgId), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "组织层启用任务")
    @PutMapping("/{id}/enable")
    public void enable(@PathVariable("organization_id") long orgId,
                       @PathVariable long id, @RequestParam long objectVersionNumber) {
        scheduleTaskService.enable(id, objectVersionNumber, ResourceLevel.ORGANIZATION.value(), orgId);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "组织层停用任务")
    @PutMapping("/{id}/disable")
    public void disable(@PathVariable("organization_id") long orgId,
                        @PathVariable long id, @RequestParam long objectVersionNumber) {
        scheduleTaskService.getQuartzTask(id, ResourceLevel.ORGANIZATION.value(), orgId);
        scheduleTaskService.disable(id, objectVersionNumber, false);
    }

    @Permission(permissionWithin = true)
    @ApiOperation(value = "停用指定组织下所有任务")
    @PutMapping("/disable")
    public void disableByOrganizationId(@PathVariable("organization_id") long orgId) {
        scheduleTaskService.disableByOrganizationId(orgId);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "组织层删除任务")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable("organization_id") long orgId,
                       @PathVariable long id) {
        scheduleTaskService.delete(id, ResourceLevel.ORGANIZATION.value(), orgId);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping
    @ApiOperation(value = "组织层分页查询定时任务")
    @CustomPageRequest
    @ResponseBody
    public ResponseEntity<Page<QuartzTaskDTO>> pagingQuery(@PathVariable("organization_id") long orgId,
                                                           @RequestParam(value = "status", required = false) String status,
                                                           @RequestParam(name = "name", required = false) String name,
                                                           @RequestParam(name = "description", required = false) String description,
                                                           @RequestParam(name = "params", required = false) String params,
                                                           @ApiIgnore
                                                           @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest) {
        return scheduleTaskService.pageQuery(pageRequest, status, name, description, params, ResourceLevel.ORGANIZATION.value(), orgId);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/{id}")
    @ApiOperation(value = "组织层查看任务详情")
    public ResponseEntity<ScheduleTaskDetailDTO> getTaskDetail(@PathVariable("organization_id") long orgId,
                                                               @PathVariable long id) {
        return new ResponseEntity<>(scheduleTaskService.getTaskDetail(id, ResourceLevel.ORGANIZATION.value(), orgId), HttpStatus.OK);

    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "组织层任务名校验")
    @PostMapping(value = "/check")
    public ResponseEntity check(@PathVariable("organization_id") long orgId,
                                @RequestBody String name) {
        scheduleTaskService.checkName(name, ResourceLevel.ORGANIZATION.value(),orgId);
        return new ResponseEntity(HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "组织层Cron表达式校验")
    @PostMapping(value = "/cron")
    public ResponseEntity<List<String>> cron(@PathVariable("organization_id") long orgId,
                                             @RequestBody String cron) {
        return new ResponseEntity(TriggerUtils.getRecentThree(cron), HttpStatus.OK);
    }
}
