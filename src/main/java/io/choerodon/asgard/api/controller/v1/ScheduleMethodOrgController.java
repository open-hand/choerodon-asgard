package io.choerodon.asgard.api.controller.v1;

import io.choerodon.asgard.api.vo.ScheduleMethod;
import io.choerodon.asgard.api.vo.ScheduleMethodInfo;
import io.choerodon.asgard.api.vo.ScheduleMethodParams;
import io.choerodon.asgard.app.service.ScheduleMethodService;
import io.choerodon.core.domain.Page;
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
@RequestMapping("/v1/schedules/organizations/{organization_id}/methods")
@Api("组织层定时任务执行方法接口")
public class ScheduleMethodOrgController {

    private ScheduleMethodService scheduleMethodService;

    public ScheduleMethodOrgController(ScheduleMethodService scheduleMethodService) {
        this.scheduleMethodService = scheduleMethodService;
    }

    public void setScheduleMethodService(ScheduleMethodService scheduleMethodService) {
        this.scheduleMethodService = scheduleMethodService;
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping
    @ApiOperation(value = "组织层分页查询执行方法列表")
    @ResponseBody
    @CustomPageRequest
    public ResponseEntity<Page<ScheduleMethodInfo>> pagingQuery(
            @PathVariable("organization_id") Long orgId,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(name = "service", required = false) String service,
            @RequestParam(name = "method", required = false) String method,
            @RequestParam(name = "description", required = false) String description,
            @RequestParam(name = "params", required = false) String params,
            @ApiIgnore
            @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest
    ) {
        return scheduleMethodService.pageQuery(pageRequest, code, service, method, description, params, ResourceLevel.ORGANIZATION.value());
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "组织层根据服务名获取方法")
    @GetMapping("/service")
    public ResponseEntity<List<ScheduleMethod>> getMethodByService(
            @PathVariable("organization_id") Long orgId,
            @RequestParam(value = "service") String service) {
        return new ResponseEntity<>(scheduleMethodService.getMethodByService(service, ResourceLevel.ORGANIZATION.value()), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/{id}")
    @ApiOperation(value = "组织层查看可执行程序详情")
    public ResponseEntity<ScheduleMethodParams> getParams(
            @PathVariable("organization_id") Long orgId,
            @Encrypt
            @PathVariable Long id) {
        return new ResponseEntity<>(scheduleMethodService.getParams(id, ResourceLevel.ORGANIZATION.value()), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "组织层搜索有可执行任务的服务名")
    @GetMapping("/services")
    public ResponseEntity<List<String>> getServices(
            @PathVariable("organization_id") Long orgId) {
        return new ResponseEntity<>(scheduleMethodService.getServices(ResourceLevel.ORGANIZATION.value()), HttpStatus.OK);
    }
}
