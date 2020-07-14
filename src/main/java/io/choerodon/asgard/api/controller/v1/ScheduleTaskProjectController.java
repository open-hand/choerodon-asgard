package io.choerodon.asgard.api.controller.v1;

import io.choerodon.asgard.api.validator.ScheduleTaskValidator;
import io.choerodon.asgard.api.vo.QuartzTask;
import io.choerodon.asgard.api.vo.ScheduleTask;
import io.choerodon.asgard.api.vo.ScheduleTaskDetail;
import io.choerodon.asgard.app.service.ScheduleTaskService;
import io.choerodon.asgard.infra.dto.QuartzTaskDTO;
import io.choerodon.asgard.infra.utils.TriggerUtils;
import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import io.choerodon.mybatis.pagehelper.domain.PageRequest;

import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/v1/schedules/projects/{project_id}/tasks")
@Api("项目层定时任务定义接口")
public class ScheduleTaskProjectController {

    private ScheduleTaskService scheduleTaskService;

    public ScheduleTaskProjectController(ScheduleTaskService scheduleTaskService) {
        this.scheduleTaskService = scheduleTaskService;
    }

    public void setScheduleTaskService(ScheduleTaskService scheduleTaskService) {
        this.scheduleTaskService = scheduleTaskService;
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目层创建定时任务")
    @PostMapping
    public ResponseEntity<QuartzTaskDTO> create(
            @Encrypt
            @PathVariable("project_id") Long projectId,
            @RequestBody @Valid ScheduleTask dto) {
        ScheduleTaskValidator.validatorCreate(dto);
        return new ResponseEntity<>(scheduleTaskService.create(dto, ResourceLevel.PROJECT.value(), projectId), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目层启用任务")
    @PutMapping("/{id}/enable")
    public void enable(
            @Encrypt
            @PathVariable("project_id") Long projectId,
            @Encrypt
            @PathVariable Long id,
            @RequestParam Long objectVersionNumber) {
        scheduleTaskService.enable(id, objectVersionNumber, ResourceLevel.PROJECT.value(), projectId);
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目层停用任务")
    @PutMapping("/{id}/disable")
    public void disable(
            @Encrypt
            @PathVariable("project_id") Long projectId,
            @Encrypt
            @PathVariable Long id,
            @RequestParam Long objectVersionNumber) {
        scheduleTaskService.getQuartzTask(id, ResourceLevel.PROJECT.value(), projectId);
        scheduleTaskService.disable(id, objectVersionNumber, false);
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目层删除任务")
    @DeleteMapping("/{id}")
    public void delete(
            @Encrypt
            @PathVariable("project_id") Long projectId,
            @Encrypt
            @PathVariable Long id) {
        scheduleTaskService.delete(id, ResourceLevel.PROJECT.value(), projectId);
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @PostMapping("/list")
    @ApiOperation(value = "项目层分页查询定时任务")
    @CustomPageRequest
    @ResponseBody
    public ResponseEntity<Page<QuartzTask>> pagingQuery(
            @Encrypt
            @PathVariable("project_id") Long projectId,
            @ApiIgnore
            @SortDefault(value = "id", direction = Sort.Direction.ASC) PageRequest pageRequest,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String params) {
        return scheduleTaskService.pageQuery(pageRequest, status, name, description, params, ResourceLevel.PROJECT.value(), projectId);
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @GetMapping("/{id}")
    @ApiOperation(value = "项目层查看任务详情")
    public ResponseEntity<ScheduleTaskDetail> getTaskDetail(
            @Encrypt
            @PathVariable("project_id") Long projectId,
            @Encrypt
            @PathVariable Long id) {
        return new ResponseEntity<>(scheduleTaskService.getTaskDetail(id, ResourceLevel.PROJECT.value(), projectId), HttpStatus.OK);

    }

    @Permission(permissionWithin = true)
    @ApiOperation(value = "停用指定项目下所有任务")
    @PutMapping("/disable")
    public void disableByProjectId(@Encrypt @PathVariable("project_id") Long projectId) {
        scheduleTaskService.disableByLevelAndSourceId(ResourceLevel.PROJECT.value(), projectId);
    }


    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目层任务名校验")
    @PostMapping(value = "/check")
    public ResponseEntity<Void> check(@Encrypt @PathVariable("project_id") Long projectId,
                                      @RequestBody String name) {
        scheduleTaskService.checkNameAllLevel(name);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目层Cron表达式校验")
    @PostMapping(value = "/cron")
    public ResponseEntity<List<String>> cron(@Encrypt @PathVariable("project_id") Long projectId,
                                             @RequestBody String cron) {
        return new ResponseEntity<>(TriggerUtils.getRecentThree(cron), HttpStatus.OK);
    }
}
