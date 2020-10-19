package io.choerodon.asgard.api.controller.v1;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hzero.core.util.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.asgard.app.service.SagaCheckStatusService;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.swagger.annotation.Permission;

/**
 * @author scp
 * @date 2020/10/19
 * @description
 */
@RestController
@RequestMapping("/v1/check")
@Api("校验saga实例有没有执行成功")
public class SagaCheckStatusController {

    @Autowired
    private SagaCheckStatusService sagaCheckStatusService;

    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping(value = "/project/{tenant_id}")
    @ApiOperation(value = "校验创项目的事务有没有执行成功")
    public ResponseEntity<Boolean> pagingQuery(@PathVariable(value = "tenant_id") Long tenantId,
                                               @RequestParam(value = "project_code") String projectCode) {
        return Results.success(sagaCheckStatusService.getCreateProjectSagaStatus(tenantId, projectCode));
    }
}
