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
@RequestMapping("/v1/schedules/tasks")
@Api("全局层定时任务定义接口")
public class ScheduleTaskSiteController {

    private ScheduleTaskService scheduleTaskService;

    public ScheduleTaskSiteController(ScheduleTaskService scheduleTaskService) {
        this.scheduleTaskService = scheduleTaskService;
    }

    public void setScheduleTaskService(ScheduleTaskService scheduleTaskService) {
        this.scheduleTaskService = scheduleTaskService;
    }

    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @ApiOperation(value = "全局层创建定时任务")
    @PostMapping
    public ResponseEntity<QuartzTaskDTO> create(@RequestBody @Valid ScheduleTask dto) {
        ScheduleTaskValidator.validatorCreate(dto);
        return new ResponseEntity<>(scheduleTaskService.create(dto, ResourceLevel.SITE.value(), 0L), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @ApiOperation(value = "全局层启用任务")
    @PutMapping("/{id}/enable")
    public void enable(@Encrypt @PathVariable Long id, @RequestParam Long objectVersionNumber) {
        scheduleTaskService.enable(id, objectVersionNumber, ResourceLevel.SITE.value(), 0L);
    }

    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @ApiOperation(value = "全局层停用任务")
    @PutMapping("/{id}/disable")
    public void disable(@Encrypt @PathVariable Long id, @RequestParam Long objectVersionNumber) {
        scheduleTaskService.getQuartzTask(id, ResourceLevel.SITE.value(), 0L);
        scheduleTaskService.disable(id, objectVersionNumber, false);
    }

    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @ApiOperation(value = "全局层删除任务")
    @DeleteMapping("/{id}")
    public void delete(@Encrypt @PathVariable Long id) {
        scheduleTaskService.delete(id, ResourceLevel.SITE.value(), 0L);
    }

    @Permission(level = ResourceLevel.SITE, permissionWithin = true)
    @ApiOperation(value = "全局层删除任务")
    @DeleteMapping("/name")
    public void deleteByName(@RequestParam(value = "name") String name) {
        scheduleTaskService.deleteByName(name, ResourceLevel.SITE.value(), 0L);
    }

    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @GetMapping
    @ApiOperation(value = "全局层分页查询定时任务")
    @CustomPageRequest
    @ResponseBody
    public ResponseEntity<Page<QuartzTask>> pagingQuery(
            @ApiIgnore
            @SortDefault(value = "id", direction = Sort.Direction.ASC) PageRequest pageRequest,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String params) {
        return scheduleTaskService.pageQuery(pageRequest, status, name, description, params, ResourceLevel.SITE.value(), 0L);
    }

    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @GetMapping("/{id}")
    @ApiOperation(value = "全局层查看任务详情")
    public ResponseEntity<ScheduleTaskDetail> getTaskDetail(@Encrypt @PathVariable Long id) {
        return new ResponseEntity<>(scheduleTaskService.getTaskDetail(id, ResourceLevel.SITE.value(), 0L), HttpStatus.OK);

    }

    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @ApiOperation(value = "全局层任务名校验")
    @PostMapping(value = "/check")
    public ResponseEntity<Void> check(@RequestBody String name) {
        scheduleTaskService.checkNameAllLevel(name);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Permission(permissionPublic = true)
    @ApiOperation(value = "全局层Cron表达式校验")
    @PostMapping(value = "/cron")
    public ResponseEntity<List<String>> cron(@RequestBody String cron) {
        return new ResponseEntity<>(TriggerUtils.getRecentThree(cron), HttpStatus.OK);
    }
}
