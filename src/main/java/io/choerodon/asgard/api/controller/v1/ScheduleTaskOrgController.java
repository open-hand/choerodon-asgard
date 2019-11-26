package io.choerodon.asgard.api.controller.v1;

import java.util.List;
import javax.validation.Valid;

import com.github.pagehelper.PageInfo;
import io.choerodon.asgard.api.vo.*;
import io.choerodon.asgard.app.service.ScheduleTaskService;
import io.choerodon.asgard.infra.dto.QuartzTaskDTO;
import io.choerodon.core.annotation.Permission;
import io.choerodon.core.enums.ResourceType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.choerodon.asgard.api.validator.ScheduleTaskValidator;
import io.choerodon.asgard.infra.utils.TriggerUtils;
import io.choerodon.core.iam.ResourceLevel;
import springfox.documentation.annotations.ApiIgnore;

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

    @Permission(type = ResourceType.ORGANIZATION)
    @ApiOperation(value = "组织层创建定时任务")
    @PostMapping
    public ResponseEntity<QuartzTaskDTO> create(@PathVariable("organization_id") long orgId,
                                                @RequestBody @Valid ScheduleTask dto) {
        ScheduleTaskValidator.validatorCreate(dto);
        return new ResponseEntity<>(scheduleTaskService.create(dto, ResourceLevel.ORGANIZATION.value(), orgId), HttpStatus.OK);
    }

    @Permission(type = ResourceType.ORGANIZATION)
    @ApiOperation(value = "组织层启用任务")
    @PutMapping("/{id}/enable")
    public void enable(@PathVariable("organization_id") long orgId,
                       @PathVariable long id, @RequestParam(value = "objectVersionNumber") long objectVersionNumber) {
        scheduleTaskService.enable(id, objectVersionNumber, ResourceLevel.ORGANIZATION.value(), orgId);
    }

    @Permission(type = ResourceType.ORGANIZATION)
    @ApiOperation(value = "组织层停用任务")
    @PutMapping("/{id}/disable")
    public void disable(@PathVariable("organization_id") long orgId,
                        @PathVariable long id, @RequestParam(value = "objectVersionNumber") long objectVersionNumber) {
        scheduleTaskService.getQuartzTask(id, ResourceLevel.ORGANIZATION.value(), orgId);
        scheduleTaskService.disable(id, objectVersionNumber, false);
    }

    @Permission(permissionWithin = true)
    @ApiOperation(value = "停用指定组织下所有任务")
    @PutMapping("/disable")
    public void disableByOrganizationId(@PathVariable("organization_id") long orgId) {
        scheduleTaskService.disableByLevelAndSourceId(ResourceLevel.ORGANIZATION.value(), orgId);
    }

    @Permission(type = ResourceType.ORGANIZATION)
    @ApiOperation(value = "组织层删除任务")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable("organization_id") long orgId,
                       @PathVariable long id) {
        scheduleTaskService.delete(id, ResourceLevel.ORGANIZATION.value(), orgId);
    }

    @Permission(type = ResourceType.ORGANIZATION)
    @GetMapping
    @ApiOperation(value = "组织层分页查询定时任务")
    @CustomPageRequest
    @ResponseBody
    public ResponseEntity<PageInfo<QuartzTask>> pagingQuery(@PathVariable("organization_id") long orgId,
                                                            @RequestParam(required = false) String status,
                                                            @RequestParam(required = false) String name,
                                                            @RequestParam(required = false) String description,
                                                            @RequestParam(required = false) String params,
                                                            @ApiIgnore
                                                            @SortDefault(value = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return scheduleTaskService.pageQuery(pageable, status, name, description, params, ResourceLevel.ORGANIZATION.value(), orgId);
    }

    @Permission(type = ResourceType.ORGANIZATION)
    @GetMapping("/{id}")
    @ApiOperation(value = "组织层查看任务详情")
    public ResponseEntity<ScheduleTaskDetail> getTaskDetail(@PathVariable("organization_id") long orgId,
                                                            @PathVariable long id) {
        return new ResponseEntity<>(scheduleTaskService.getTaskDetail(id, ResourceLevel.ORGANIZATION.value(), orgId), HttpStatus.OK);

    }

    @Permission(type = ResourceType.ORGANIZATION)
    @ApiOperation(value = "组织层任务名校验")
    @PostMapping(value = "/check")
    public ResponseEntity check(@PathVariable("organization_id") long orgId,
                                @RequestBody String name) {
        scheduleTaskService.checkNameAllLevel(name);
        return new ResponseEntity(HttpStatus.OK);
    }

    @Permission(type = ResourceType.ORGANIZATION)
    @ApiOperation(value = "组织层Cron表达式校验")
    @PostMapping(value = "/cron")
    public ResponseEntity<List<String>> cron(@PathVariable("organization_id") long orgId,
                                             @RequestBody String cron) {
        return new ResponseEntity(TriggerUtils.getRecentThree(cron), HttpStatus.OK);
    }
}
