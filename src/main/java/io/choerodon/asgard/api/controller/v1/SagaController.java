package io.choerodon.asgard.api.controller.v1;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hzero.core.util.Results;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.asgard.api.vo.Saga;
import io.choerodon.asgard.api.vo.SagaWithTask;
import io.choerodon.asgard.app.service.SagaService;
import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

@RestController
@RequestMapping("/v1/sagas")
@Api("saga定义接口")
public class SagaController {

    private SagaService sagaService;

    public SagaController(SagaService sagaService) {
        this.sagaService = sagaService;
    }

    public void setSagaService(SagaService sagaService) {
        this.sagaService = sagaService;
    }

    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @GetMapping
    @ApiOperation(value = "查询事务列表")
    @CustomPageRequest
    public ResponseEntity<Page<Saga>> pagingQuery(@ApiIgnore
                                                      @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest,
                                                  @RequestParam(required = false) String code,
                                                  @RequestParam(required = false) String description,
                                                  @RequestParam(required = false) String service,
                                                  @RequestParam(required = false) String params) {
        return sagaService.pagingQuery(pageRequest, code, description, service, params);
    }

    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @GetMapping("/{id}")
    @ApiOperation(value = "查询某个事务的定义详情")
    public ResponseEntity<SagaWithTask> query(@Encrypt @PathVariable("id") Long id) {
        return sagaService.query(id);
    }


    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除事务")
    public void delete(@Encrypt @PathVariable("id") Long id) {
        sagaService.delete(id);
    }


    @ApiOperation("手动刷新事务实例")
    @Permission(permissionPublic = true)
    @PostMapping(value = "/fresh")
    public ResponseEntity<Void> refresh(@RequestParam("serviceName") String serviceName) {
        sagaService.refresh(serviceName);
        return Results.success();
    }

}
