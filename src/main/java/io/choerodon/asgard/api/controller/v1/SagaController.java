package io.choerodon.asgard.api.controller.v1;

import io.choerodon.base.annotation.Permission;
import io.choerodon.base.enums.ResourceType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.asgard.api.dto.SagaDTO;
import io.choerodon.asgard.api.dto.SagaWithTaskDTO;
import io.choerodon.asgard.api.service.SagaService;
import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.CustomPageRequest;

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
    public ResponseEntity<Page<SagaDTO>> pagingQuery(@RequestParam(required = false, name = "code") String code,
                                                     @RequestParam(required = false, name = "description") String description,
                                                     @RequestParam(required = false, name = "service") String service,
                                                     @RequestParam(required = false, name = "params") String params,
                                                     @ApiIgnore
                                                     @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest) {
        return sagaService.pagingQuery(pageRequest, code, description, service, params);
    }

    @Permission(type = ResourceType.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @GetMapping("/{id}")
    @ApiOperation(value = "查询某个事务的定义详情")
    public ResponseEntity<SagaWithTaskDTO> query(@PathVariable("id") Long id) {
        return sagaService.query(id);
    }


    @Permission(type = ResourceType.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除事务")
    public void delete(@PathVariable("id") Long id) {
        sagaService.delete(id);
    }


}
