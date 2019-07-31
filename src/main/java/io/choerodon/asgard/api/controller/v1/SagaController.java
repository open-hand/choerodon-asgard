package io.choerodon.asgard.api.controller.v1;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.annotation.Permission;
import io.choerodon.base.constant.PageConstant;
import io.choerodon.base.enums.ResourceType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.asgard.api.vo.Saga;
import io.choerodon.asgard.api.vo.SagaWithTask;
import io.choerodon.asgard.app.service.SagaService;
import io.choerodon.core.iam.InitRoleCode;

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

    @Permission(type = ResourceType.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @GetMapping
    @ApiOperation(value = "查询事务列表")
    public ResponseEntity<PageInfo<Saga>> pagingQuery(@RequestParam(required = false, name = "code") String code,
                                                      @RequestParam(required = false, name = "description") String description,
                                                      @RequestParam(required = false, name = "service") String service,
                                                      @RequestParam(required = false, name = "params") String params,
                                                      @RequestParam(defaultValue = PageConstant.PAGE, required = false) final int page,
                                                      @RequestParam(defaultValue = PageConstant.SIZE, required = false) final int size) {
        return sagaService.pagingQuery(page, size, code, description, service, params);
    }

    @Permission(type = ResourceType.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @GetMapping("/{id}")
    @ApiOperation(value = "查询某个事务的定义详情")
    public ResponseEntity<SagaWithTask> query(@PathVariable("id") Long id) {
        return sagaService.query(id);
    }


    @Permission(type = ResourceType.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除事务")
    public void delete(@PathVariable("id") Long id) {
        sagaService.delete(id);
    }


}
