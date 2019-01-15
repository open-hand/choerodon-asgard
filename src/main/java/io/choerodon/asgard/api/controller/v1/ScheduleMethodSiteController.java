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
@Api("全局层定时任务执行方法接口")
public class ScheduleMethodSiteController {

    private ScheduleMethodService scheduleMethodService;

    public ScheduleMethodSiteController(ScheduleMethodService scheduleMethodService) {
        this.scheduleMethodService = scheduleMethodService;
    }

    public void setScheduleMethodService(ScheduleMethodService scheduleMethodService) {
        this.scheduleMethodService = scheduleMethodService;
    }

    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @GetMapping
    @ApiOperation(value = "全局层分页查询执行方法列表")
    @CustomPageRequest
    @ResponseBody
    public ResponseEntity<Page<ScheduleMethodInfoDTO>> pagingQuery(@RequestParam(value = "code", required = false) String code,
                                                                   @RequestParam(name = "service", required = false) String service,
                                                                   @RequestParam(name = "method", required = false) String method,
                                                                   @RequestParam(name = "description", required = false) String description,
                                                                   @RequestParam(name = "params", required = false) String params,
                                                                   @ApiIgnore
                                                                   @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest) {
        return scheduleMethodService.pageQuery(pageRequest, code, service, method, description, params, null);
    }


    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @ApiOperation(value = "全局层根据服务名获取方法")
    @GetMapping("/service")
    public ResponseEntity<List<ScheduleMethodDTO>> getMethodByService(@RequestParam(value = "service") String service) {
        return new ResponseEntity<>(scheduleMethodService.getMethodByService(service, ResourceLevel.SITE.value()), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @GetMapping("/{id}")
    @ApiOperation(value = "全局层查看可执行程序详情")
    public ResponseEntity<ScheduleMethodParamsDTO> getParams(@PathVariable long id) {
        return new ResponseEntity<>(scheduleMethodService.getParams(id, ResourceLevel.SITE.value()), HttpStatus.OK);
    }


    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @ApiOperation(value = "平台层搜索有可执行任务的服务名")
    @GetMapping("/services")
    public ResponseEntity<List<String>> getServices() {
        return new ResponseEntity<>(scheduleMethodService.getServices(ResourceLevel.SITE.value()), HttpStatus.OK);
    }

    @Permission(permissionWithin = true)
    @ApiOperation(value = "根据方法code检索可执行程序方法Id")
    @GetMapping("/code/{code}")
    public ResponseEntity<Long> getMethodIdByCode(@PathVariable String code) {
        return new ResponseEntity<>(scheduleMethodService.getMethodIdByCode(code), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_ADMINISTRATOR})
    @ApiOperation(value = "删除平台层可执行程序")
    @DeleteMapping("/{id}")
    public ResponseEntity delete(@PathVariable Long id) {
        scheduleMethodService.delete(id);
        return new ResponseEntity(HttpStatus.OK);
    }
}
