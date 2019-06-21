package io.choerodon.asgard.api.controller.v1;

import java.util.List;
import javax.validation.Valid;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.annotation.Permission;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.base.domain.Sort;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.mybatis.annotation.SortDefault;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.asgard.api.dto.QuartzTaskDTO;
import io.choerodon.asgard.api.dto.ScheduleTaskDTO;
import io.choerodon.asgard.api.dto.ScheduleTaskDetailDTO;
import io.choerodon.asgard.api.service.ScheduleTaskService;
import io.choerodon.asgard.api.validator.ScheduleTaskValidator;
import io.choerodon.asgard.domain.QuartzTask;
import io.choerodon.asgard.infra.utils.TriggerUtils;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import springfox.documentation.annotations.ApiIgnore;

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

    @Permission(type = ResourceType.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @ApiOperation(value = "全局层创建定时任务")
    @PostMapping
    public ResponseEntity<QuartzTask> create(@RequestBody @Valid ScheduleTaskDTO dto) {
        ScheduleTaskValidator.validatorCreate(dto);
        return new ResponseEntity<>(scheduleTaskService.create(dto, ResourceLevel.SITE.value(), 0L), HttpStatus.OK);
    }

    @Permission(type = ResourceType.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @ApiOperation(value = "全局层启用任务")
    @PutMapping("/{id}/enable")
    public void enable(@PathVariable long id, @RequestParam long objectVersionNumber) {
        scheduleTaskService.enable(id, objectVersionNumber, ResourceLevel.SITE.value(), 0L);
    }

    @Permission(type = ResourceType.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @ApiOperation(value = "全局层停用任务")
    @PutMapping("/{id}/disable")
    public void disable(@PathVariable long id, @RequestParam long objectVersionNumber) {
        scheduleTaskService.getQuartzTask(id, ResourceLevel.SITE.value(), 0L);
        scheduleTaskService.disable(id, objectVersionNumber, false);
    }

    @Permission(type = ResourceType.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @ApiOperation(value = "全局层删除任务")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable long id) {
        scheduleTaskService.delete(id, ResourceLevel.SITE.value(), 0L);
    }

    @Permission(type = ResourceType.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @GetMapping
    @ApiOperation(value = "全局层分页查询定时任务")
    @CustomPageRequest
    @ResponseBody
    public ResponseEntity<PageInfo<QuartzTaskDTO>> pagingQuery(@RequestParam(value = "status", required = false) String status,
                                                               @RequestParam(name = "name", required = false) String name,
                                                               @RequestParam(name = "description", required = false) String description,
                                                               @RequestParam(name = "params", required = false) String params,
                                                               @ApiIgnore
                                                               @SortDefault(value = "id", direction = Sort.Direction.ASC) PageRequest pageRequest) {
        return scheduleTaskService.pageQuery(pageRequest, status, name, description, params, ResourceLevel.SITE.value(), 0L);
    }

    @Permission(type = ResourceType.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @GetMapping("/{id}")
    @ApiOperation(value = "全局层查看任务详情")
    public ResponseEntity<ScheduleTaskDetailDTO> getTaskDetail(@PathVariable long id) {
        return new ResponseEntity<>(scheduleTaskService.getTaskDetail(id, ResourceLevel.SITE.value(), 0L), HttpStatus.OK);

    }

    @Permission(type = ResourceType.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @ApiOperation(value = "全局层任务名校验")
    @PostMapping(value = "/check")
    public ResponseEntity check(@RequestBody String name) {
        scheduleTaskService.checkNameAllLevel(name);
        return new ResponseEntity(HttpStatus.OK);
    }

    @Permission(type = ResourceType.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @ApiOperation(value = "全局层Cron表达式校验")
    @PostMapping(value = "/cron")
    public ResponseEntity<List<String>> cron(@RequestBody String cron) {
        return new ResponseEntity<>(TriggerUtils.getRecentThree(cron), HttpStatus.OK);
    }
}
