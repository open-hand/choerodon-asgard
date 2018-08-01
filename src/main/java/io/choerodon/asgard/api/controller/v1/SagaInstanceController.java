package io.choerodon.asgard.api.controller.v1;

import io.choerodon.asgard.api.dto.SagaInstanceDTO;
import io.choerodon.asgard.api.dto.SagaWithTaskInstanceDTO;
import io.choerodon.asgard.api.dto.StartInstanceDTO;
import io.choerodon.asgard.api.service.SagaInstanceService;
import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1/sagas/instances")
public class SagaInstanceController {

    private SagaInstanceService sagaInstanceService;

    public SagaInstanceController(SagaInstanceService sagaInstanceService) {
        this.sagaInstanceService = sagaInstanceService;
    }

    /**
     * 内部接口。生产者端通过feign调用该接口
     * 开始执行一个saga
     */
    @PostMapping("/{code:.*}")
    @ApiOperation(value = "内部接口。开始一个saga")
    public ResponseEntity<SagaInstanceDTO> start(@PathVariable("code") String code,
                                                 @RequestBody @Valid StartInstanceDTO dto) {
        dto.setSagaCode(code);
        return sagaInstanceService.start(dto);
    }

    @Permission(level = ResourceLevel.SITE)
    @GetMapping
    @ApiOperation(value = "查询事务实例列表")
    @CustomPageRequest
    public ResponseEntity<Page<SagaInstanceDTO>> pagingQuery(@RequestParam(value = "sagaCode", required = false) String sagaCode,
                                                             @RequestParam(name = "status", required = false) String status,
                                                             @RequestParam(name = "refType", required = false) String refType,
                                                             @RequestParam(name = "refId", required = false) String refId,
                                                             @RequestParam(required = false, name = "params") String params,
                                                             @ApiIgnore
                                                             @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest) {
        return sagaInstanceService.pageQuery(pageRequest, sagaCode, status, refType, refId, params);
    }

    @Permission(level = ResourceLevel.SITE)
    @GetMapping("/{id}")
    @ApiOperation(value = "查询某个事务实例运行详情")
    public ResponseEntity<SagaWithTaskInstanceDTO> query(@PathVariable("id") Long id) {
        return sagaInstanceService.query(id);
    }


}
