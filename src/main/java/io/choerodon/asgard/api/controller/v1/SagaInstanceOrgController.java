package io.choerodon.asgard.api.controller.v1;


import io.choerodon.asgard.api.vo.SagaInstanceDetails;
import io.choerodon.asgard.api.vo.SagaInstanceFailureVO;
import io.choerodon.asgard.api.vo.SagaWithTaskInstance;
import io.choerodon.asgard.app.service.SagaInstanceService;
import io.choerodon.asgard.infra.dto.SagaInstanceDTO;
import io.choerodon.asgard.infra.utils.KeyDecryptHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

import io.swagger.annotations.ApiOperation;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/v1/sagas/organizations/{organization_id}/instances")
public class SagaInstanceOrgController {

    private SagaInstanceService sagaInstanceService;

    public SagaInstanceOrgController(SagaInstanceService sagaInstanceService) {
        this.sagaInstanceService = sagaInstanceService;
    }

    public void setSagaInstanceService(SagaInstanceService sagaInstanceService) {
        this.sagaInstanceService = sagaInstanceService;
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @GetMapping
    @ApiOperation(value = "组织层查询事务实例列表")
    @ResponseBody
    @CustomPageRequest
    public ResponseEntity<Page<SagaInstanceDetails>> pagingQuery(
            @ApiIgnore
            @SortDefault(value = "id", direction = Sort.Direction.ASC) PageRequest pageRequest,
            @PathVariable("organization_id") long orgId,
            @RequestParam(required = false) String sagaCode,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String refType,
            @RequestParam(required = false) String refId,
            @RequestParam(required = false) String params,
            @RequestParam(required = false) String searchId) {
        Long searchIdNum = null;
        if (!StringUtils.isEmpty(searchId)) {
            searchIdNum = Long.valueOf(searchId);
        }
        return sagaInstanceService.pageQuery(pageRequest, KeyDecryptHelper.decryptSagaCode(sagaCode), status, refType, refId, params, ResourceLevel.ORGANIZATION.value(), orgId, searchIdNum);
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @GetMapping(value = "/{id}", produces = "application/json")
    @ApiOperation(value = "组织层查询某个事务实例运行详情")
    public ResponseEntity<SagaWithTaskInstance> query(
            @PathVariable("organization_id") long orgId,
            @Encrypt
            @PathVariable("id") Long id) {
        return new ResponseEntity<>(sagaInstanceService.query(id), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @GetMapping(value = "/{id}/details", produces = "application/json")
    @ApiOperation(value = "组织层查询事务实例的具体信息")
    public ResponseEntity<SagaInstanceDetails> queryDetails(
            @PathVariable("organization_id") long orgId,
            @Encrypt
            @PathVariable("id") Long id) {
        return new ResponseEntity<>(sagaInstanceService.queryDetails(id), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @GetMapping(value = "/statistics", produces = "application/json")
    @ApiOperation(value = "统计组织下各个事务实例状态下的实例个数")
    public ResponseEntity<Map<String, Integer>> statistics(@PathVariable("organization_id") long orgId) {
        return new ResponseEntity<>(sagaInstanceService.statistics(ResourceLevel.ORGANIZATION.value(), orgId), HttpStatus.OK);
    }


    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @GetMapping(value = "/statistics/failure")
    @ApiOperation(value = "统计组织下失败实例情况")
    public ResponseEntity<List<SagaInstanceFailureVO>> statisticsFailure(
            @PathVariable("organization_id") long orgId,
            @RequestParam("date") Integer date) {
        return new ResponseEntity<>(sagaInstanceService.statisticsFailure(ResourceLevel.ORGANIZATION.value(), orgId, date), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @GetMapping(value = "/statistics/failure/list")
    @CustomPageRequest
    @ApiOperation(value = "统计组织下失败实例情况详情")
    public ResponseEntity<Page<SagaInstanceDTO>> statisticsFailureList(
            @ApiIgnore
            @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest PageRequest,
            @PathVariable("organization_id") long orgId,
            @RequestParam("date") Integer date) {
        return new ResponseEntity<>(sagaInstanceService.statisticsFailureList(ResourceLevel.ORGANIZATION.value(), orgId, date, PageRequest), HttpStatus.OK);
    }

}
