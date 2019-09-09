package io.choerodon.asgard.api.controller.v1;

import java.util.List;
import javax.validation.Valid;

import com.github.pagehelper.PageInfo;
import io.choerodon.asgard.api.vo.ScheduleTaskSiteSearchVO;
import io.choerodon.asgard.infra.dto.QuartzTaskDTO;
import io.choerodon.base.annotation.Permission;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.base.domain.Sort;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.mybatis.annotation.SortDefault;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.asgard.api.vo.QuartzTask;
import io.choerodon.asgard.api.vo.ScheduleTask;
import io.choerodon.asgard.api.vo.ScheduleTaskDetail;
import io.choerodon.asgard.app.service.ScheduleTaskService;
import io.choerodon.asgard.api.validator.ScheduleTaskValidator;
import io.choerodon.asgard.infra.utils.TriggerUtils;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.swagger.annotation.CustomPageRequest;
import springfox.documentation.annotations.ApiIgnore;

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

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目层创建定时任务")
    @PostMapping
    public ResponseEntity<QuartzTaskDTO> create(@PathVariable("project_id") long projectId,
                                                @RequestBody @Valid ScheduleTask dto) {
        ScheduleTaskValidator.validatorCreate(dto);
        return new ResponseEntity<>(scheduleTaskService.create(dto, ResourceLevel.PROJECT.value(), projectId), HttpStatus.OK);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目层启用任务")
    @PutMapping("/{id}/enable")
    public void enable(@PathVariable("project_id") long projectId,
                       @PathVariable long id, @RequestParam long objectVersionNumber) {
        scheduleTaskService.enable(id, objectVersionNumber, ResourceLevel.PROJECT.value(), projectId);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目层停用任务")
    @PutMapping("/{id}/disable")
    public void disable(@PathVariable("project_id") long projectId,
                        @PathVariable long id, @RequestParam long objectVersionNumber) {
        scheduleTaskService.getQuartzTask(id, ResourceLevel.PROJECT.value(), projectId);
        scheduleTaskService.disable(id, objectVersionNumber, false);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目层删除任务")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable("project_id") long projectId,
                       @PathVariable long id) {
        scheduleTaskService.delete(id, ResourceLevel.PROJECT.value(), projectId);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @PostMapping("/list")
    @ApiOperation(value = "项目层分页查询定时任务")
    @CustomPageRequest
    @ResponseBody
    public ResponseEntity<PageInfo<QuartzTask>> pagingQuery(@PathVariable("project_id") long projectId,
                                                            @RequestBody ScheduleTaskSiteSearchVO scheduleTaskSiteSearchVO,
                                                            @ApiIgnore
                                                               @SortDefault(value = "id", direction = Sort.Direction.ASC) PageRequest pageRequest) {
        return scheduleTaskService.pageQuery(pageRequest, scheduleTaskSiteSearchVO, ResourceLevel.PROJECT.value(), projectId);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @GetMapping("/{id}")
    @ApiOperation(value = "项目层查看任务详情")
    public ResponseEntity<ScheduleTaskDetail> getTaskDetail(@PathVariable("project_id") long projectId,
                                                            @PathVariable long id) {
        return new ResponseEntity<>(scheduleTaskService.getTaskDetail(id, ResourceLevel.PROJECT.value(), projectId), HttpStatus.OK);

    }

    @Permission(permissionWithin = true)
    @ApiOperation(value = "停用指定项目下所有任务")
    @PutMapping("/disable")
    public void disableByProjectId(@PathVariable("project_id") long projectId) {
        scheduleTaskService.disableByLevelAndSourceId(ResourceLevel.PROJECT.value(), projectId);
    }


    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目层任务名校验")
    @PostMapping(value = "/check")
    public ResponseEntity check(@PathVariable("project_id") long projectId,
                                @RequestBody String name) {
        scheduleTaskService.checkNameAllLevel(name);
        return new ResponseEntity(HttpStatus.OK);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目层Cron表达式校验")
    @PostMapping(value = "/cron")
    public ResponseEntity<List<String>> cron(@PathVariable("project_id") long projectId,
                                             @RequestBody String cron) {
        return new ResponseEntity(TriggerUtils.getRecentThree(cron), HttpStatus.OK);
    }
}
