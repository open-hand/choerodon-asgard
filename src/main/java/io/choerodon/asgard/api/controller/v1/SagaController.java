package io.choerodon.asgard.api.controller.v1;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.annotation.Permission;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.base.domain.Sort;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.mybatis.annotation.SortDefault;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.asgard.api.vo.Saga;
import io.choerodon.asgard.api.vo.SagaWithTask;
import io.choerodon.asgard.app.service.SagaService;
import io.choerodon.core.iam.InitRoleCode;
import springfox.documentation.annotations.ApiIgnore;

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
    @CustomPageRequest
    public ResponseEntity<PageInfo<Saga>> pagingQuery(@ApiIgnore
                                                      @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest,
                                                      @RequestParam(required = false) String code,
                                                      @RequestParam(required = false) String description,
                                                      @RequestParam(required = false) String service,
                                                      @RequestParam(required = false) String params) {
        return sagaService.pagingQuery(pageRequest, code, description, service, params);
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
