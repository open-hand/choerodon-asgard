package io.choerodon.asgard.api.controller.v1;

import io.choerodon.asgard.api.vo.SagaInstanceDetails;
import io.choerodon.asgard.api.vo.SagaInstanceFailureVO;
import io.choerodon.asgard.api.vo.SagaWithTaskInstance;
import io.choerodon.asgard.app.service.SagaInstanceService;
import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/v1/sagas/projects/{project_id}/instances")
public class SagaInstanceProjectController {

    private SagaInstanceService sagaInstanceService;

    public SagaInstanceProjectController(SagaInstanceService sagaInstanceService) {
        this.sagaInstanceService = sagaInstanceService;
    }

    public void setSagaInstanceService(SagaInstanceService sagaInstanceService) {
        this.sagaInstanceService = sagaInstanceService;
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_ADMINISTRATOR, InitRoleCode.PROJECT_MEMBER})
    @GetMapping
    @ApiOperation(value = "项目层查询事务实例列表")
    @ResponseBody
    @CustomPageRequest
    public ResponseEntity<Page<SagaInstanceDetails>> pagingQuery(
            @PathVariable("project_id") long projectId,
            @RequestParam(required = false) String sagaCode,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String refType,
            @RequestParam(required = false) String refId,
            @RequestParam(required = false) String params,
            @ApiIgnore
            @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest) {
        return sagaInstanceService.pageQuery(pageRequest, sagaCode, status, refType, refId, params, ResourceLevel.PROJECT.value(), projectId);
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_ADMINISTRATOR, InitRoleCode.PROJECT_MEMBER})
    @GetMapping(value = "/{id}", produces = "application/json")
    @ApiOperation(value = "项目层查询某个事务实例运行详情")
    public ResponseEntity<SagaWithTaskInstance> query(
            @PathVariable("project_id") long projectId,
            @Encrypt
            @PathVariable("id") Long id) {
        return new ResponseEntity<>(sagaInstanceService.query(id), HttpStatus.OK);
    }


    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_ADMINISTRATOR, InitRoleCode.PROJECT_MEMBER})
    @GetMapping(value = "/{id}/details", produces = "application/json")
    @ApiOperation(value = "项目层查询事务实例的具体信息")
    public ResponseEntity<SagaInstanceDetails> queryDetails(
            @PathVariable("project_id") long projectId,
            @Encrypt
            @PathVariable("id") Long id) {
        return new ResponseEntity<>(sagaInstanceService.queryDetails(id), HttpStatus.OK);
    }


    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_ADMINISTRATOR, InitRoleCode.PROJECT_MEMBER})
    @GetMapping(value = "/statistics", produces = "application/json")
    @ApiOperation(value = "统计项目下各个事务实例状态下的实例个数")
    public ResponseEntity<Map<String, Integer>> statistics(
            @PathVariable("project_id") long projectId) {
        return new ResponseEntity<>(sagaInstanceService.statistics(ResourceLevel.PROJECT.value(), projectId), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @GetMapping(value = "/statistics/failure")
    @ApiOperation(value = "统计项目下失败实例情况")
    public ResponseEntity<List<SagaInstanceFailureVO>> statisticsFailure(
            @ApiParam(value = "项目id", required = true)
            @PathVariable("project_id") Long projectId,
            @ApiParam(value = "时间范围", required = true)
            @RequestParam("date") Integer date) {
        return new ResponseEntity<>(sagaInstanceService.statisticsFailure(ResourceLevel.PROJECT.value(), projectId, date), HttpStatus.OK);
    }

}
