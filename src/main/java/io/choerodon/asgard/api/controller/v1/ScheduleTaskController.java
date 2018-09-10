package io.choerodon.asgard.api.controller.v1;

import io.choerodon.asgard.api.dto.QuartzTaskDTO;
import io.choerodon.asgard.api.dto.ScheduleTaskDTO;
import io.choerodon.asgard.api.dto.TriggerType;
import io.choerodon.asgard.api.service.ScheduleTaskService;
import io.choerodon.asgard.domain.QuartzTask;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.quartz.CronExpression;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.HashMap;

@RestController
@RequestMapping("/v1/schedules/tasks")
@Api("定时任务定义接口")
public class ScheduleTaskController {

    private ScheduleTaskService scheduleTaskService;

    public ScheduleTaskController(ScheduleTaskService scheduleTaskService) {
        this.scheduleTaskService = scheduleTaskService;
    }

    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @ApiOperation(value = "创建定时任务")
    @PostMapping
    public ResponseEntity<QuartzTask> create(@RequestBody @Valid ScheduleTaskDTO dto) {
        if (dto.getParams() == null) {
            dto.setParams(new HashMap<>(0));
        }
        if (TriggerType.CRON.getValue().equals(dto.getTriggerType())) {
            if (StringUtils.isEmpty(dto.getCronExpression())) {
                throw new CommonException("error.scheduleTask.cronExpressionEmpty");
            }
            if (!CronExpression.isValidExpression(dto.getCronExpression())) {
                throw new CommonException("error.scheduleTask.cronExpressionInvalid");
            }
        } else if (TriggerType.SIMPLE.getValue().equals(dto.getTriggerType())) {
            if (dto.getSimpleRepeatInterval() == null) {
                throw new CommonException("error.scheduleTask.repeatCountOrRepeatIntervalNull");
            }
        } else {
            throw new CommonException("error.scheduleTask.invalidTriggerType");
        }
        return new ResponseEntity<>(scheduleTaskService.create(dto), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @ApiOperation(value = "启用任务")
    @PutMapping("/{id}/enable")
    public void enable(@PathVariable long id, @RequestParam long objectVersionNumber) {
       scheduleTaskService.enable(id, objectVersionNumber);
    }

    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @ApiOperation(value = "停用任务")
    @PutMapping("/{id}/disable")
    public void disable(@PathVariable long id, @RequestParam long objectVersionNumber) {
        scheduleTaskService.disable(id, objectVersionNumber, false);
    }

    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @ApiOperation(value = "删除任务")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable long id) {
        scheduleTaskService.delete(id);
    }

    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @GetMapping
    @ApiOperation(value = "分页查询定时任务")
    @CustomPageRequest
    @ResponseBody
    public ResponseEntity<Page<QuartzTaskDTO>> pagingQuery(@RequestParam(value = "status", required = false) String status,
                                                           @RequestParam(name = "name", required = false) String name,
                                                           @RequestParam(name = "description", required = false) String description,
                                                           @RequestParam(name = "params", required = false) String params,
                                                           @ApiIgnore
                                                           @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest) {
        return scheduleTaskService.pageQuery(pageRequest, status, name, description, params);
    }



}
