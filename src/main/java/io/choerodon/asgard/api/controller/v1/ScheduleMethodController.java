package io.choerodon.asgard.api.controller.v1;

import java.util.List;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.asgard.api.dto.ScheduleMethodDTO;
import io.choerodon.asgard.api.dto.ScheduleMethodInfoDTO;
import io.choerodon.asgard.api.dto.ScheduleMethodParamsDTO;
import io.choerodon.asgard.api.service.ScheduleMethodService;
import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

@RestController
@RequestMapping("/v1/schedules/methods")
@Api("定时任务执行方法接口")
public class ScheduleMethodController {

    private ScheduleMethodService scheduleMethodService;

    public ScheduleMethodController(ScheduleMethodService scheduleMethodService) {
        this.scheduleMethodService = scheduleMethodService;
    }


    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @GetMapping
    @ApiOperation(value = "分页查询执行方法列表")
    @CustomPageRequest
    @ResponseBody
    public ResponseEntity<Page<ScheduleMethodInfoDTO>> pagingQuery(@RequestParam(value = "code", required = false) String code,
                                                                   @RequestParam(name = "service", required = false) String service,
                                                                   @RequestParam(name = "method", required = false) String method,
                                                                   @RequestParam(name = "description", required = false) String description,
                                                                   @RequestParam(name = "params", required = false) String params,
                                                                   @ApiIgnore
                                                                   @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest) {
        return scheduleMethodService.pageQuery(pageRequest, code, service, method, description, params);
    }


    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @ApiOperation(value = "根据服务名获取方法")
    @GetMapping("/service")
    public ResponseEntity<List<ScheduleMethodDTO>> getMethodByService(@RequestParam(value = "service") String service) {
        return new ResponseEntity<>(scheduleMethodService.getMethodByService(service), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @GetMapping("/{id}")
    @ApiOperation(value = "查看可执行程序详情")
    public ResponseEntity<ScheduleMethodParamsDTO> getParams(@PathVariable long id) {
        return new ResponseEntity<>(scheduleMethodService.getParams(id), HttpStatus.OK);
    }
}
