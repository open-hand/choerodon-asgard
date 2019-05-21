package io.choerodon.asgard.api.controller.v1;

import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.annotation.Permission;
import io.choerodon.base.constant.PageConstant;
import io.choerodon.base.enums.ResourceType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.asgard.api.dto.ScheduleMethodDTO;
import io.choerodon.asgard.api.dto.ScheduleMethodInfoDTO;
import io.choerodon.asgard.api.dto.ScheduleMethodParamsDTO;
import io.choerodon.asgard.api.service.ScheduleMethodService;
import io.choerodon.core.iam.ResourceLevel;

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

    @Permission(type = ResourceType.ORGANIZATION)
    @GetMapping
    @ApiOperation(value = "组织层分页查询执行方法列表")
    @ResponseBody
    public ResponseEntity<PageInfo<ScheduleMethodInfoDTO>> pagingQuery(@PathVariable("organization_id") long orgId,
                                                                       @RequestParam(value = "code", required = false) String code,
                                                                       @RequestParam(name = "service", required = false) String service,
                                                                       @RequestParam(name = "method", required = false) String method,
                                                                       @RequestParam(name = "description", required = false) String description,
                                                                       @RequestParam(name = "params", required = false) String params,
                                                                       @RequestParam(defaultValue = PageConstant.PAGE, required = false) final int page,
                                                                       @RequestParam(defaultValue = PageConstant.SIZE, required = false) final int size) {
        return scheduleMethodService.pageQuery(page, size, code, service, method, description, params, ResourceLevel.ORGANIZATION.value());
    }

    @Permission(type = ResourceType.ORGANIZATION)
    @ApiOperation(value = "组织层根据服务名获取方法")
    @GetMapping("/service")
    public ResponseEntity<List<ScheduleMethodDTO>> getMethodByService(@PathVariable("organization_id") long orgId,
                                                                      @RequestParam(value = "service") String service) {
        return new ResponseEntity<>(scheduleMethodService.getMethodByService(service, ResourceLevel.ORGANIZATION.value()), HttpStatus.OK);
    }

    @Permission(type = ResourceType.ORGANIZATION)
    @GetMapping("/{id}")
    @ApiOperation(value = "组织层查看可执行程序详情")
    public ResponseEntity<ScheduleMethodParamsDTO> getParams(@PathVariable("organization_id") long orgId,
                                                             @PathVariable long id) {
        return new ResponseEntity<>(scheduleMethodService.getParams(id, ResourceLevel.ORGANIZATION.value()), HttpStatus.OK);
    }

    @Permission(type = ResourceType.ORGANIZATION)
    @ApiOperation(value = "组织层搜索有可执行任务的服务名")
    @GetMapping("/services")
    public ResponseEntity<List<String>> getServices(@PathVariable("organization_id") long orgId) {
        return new ResponseEntity<>(scheduleMethodService.getServices(ResourceLevel.ORGANIZATION.value()), HttpStatus.OK);
    }
}
