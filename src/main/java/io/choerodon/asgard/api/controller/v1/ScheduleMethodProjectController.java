package io.choerodon.asgard.api.controller.v1;

import io.choerodon.asgard.api.vo.ScheduleMethod;
import io.choerodon.asgard.api.vo.ScheduleMethodInfo;
import io.choerodon.asgard.api.vo.ScheduleMethodParams;
import io.choerodon.asgard.app.service.ScheduleMethodService;
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

import java.util.List;

@RestController
@RequestMapping("/v1/schedules/projects/{project_id}/methods")
@Api("项目层定时任务执行方法接口")
public class ScheduleMethodProjectController {

    private ScheduleMethodService scheduleMethodService;

    public ScheduleMethodProjectController(ScheduleMethodService scheduleMethodService) {
        this.scheduleMethodService = scheduleMethodService;
    }

    public void setScheduleMethodService(ScheduleMethodService scheduleMethodService) {
        this.scheduleMethodService = scheduleMethodService;
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @GetMapping
    @ApiOperation(value = "项目层分页查询执行方法列表")
    @ResponseBody
    @CustomPageRequest
    public ResponseEntity<Page<ScheduleMethodInfo>> pagingQuery(
            @PathVariable("project_id") Long projectId,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(name = "service", required = false) String service,
            @RequestParam(name = "method", required = false) String method,
            @RequestParam(name = "description", required = false) String description,
            @RequestParam(name = "params", required = false) String params,
            @ApiIgnore
            @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest) {
        return scheduleMethodService.pageQuery(pageRequest, code, service, method, description, params, ResourceLevel.PROJECT.value());
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目层根据服务名获取方法")
    @GetMapping("/service")
    public ResponseEntity<List<ScheduleMethod>> getMethodByService(
            @PathVariable("project_id") Long projectId,
            @RequestParam(value = "service") String service) {
        return new ResponseEntity<>(scheduleMethodService.getMethodByService(service, ResourceLevel.PROJECT.value()), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @GetMapping("/{id}")
    @ApiOperation(value = "项目层查看可执行程序详情")
    public ResponseEntity<ScheduleMethodParams> getParams(
            @PathVariable("project_id") Long projectId,
            @Encrypt
            @PathVariable Long id) {
        return new ResponseEntity<>(scheduleMethodService.getParams(id, ResourceLevel.PROJECT.value()), HttpStatus.OK);
    }


    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目层搜索有可执行任务的服务名")
    @GetMapping("/services")
    public ResponseEntity<List<String>> getServices(@PathVariable("project_id") Long projectId) {
        return new ResponseEntity<>(scheduleMethodService.getServices(ResourceLevel.PROJECT.value()), HttpStatus.OK);
    }
}
