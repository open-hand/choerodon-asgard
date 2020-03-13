package io.choerodon.asgard.api.controller.v1;

import com.github.pagehelper.PageInfo;
import io.choerodon.asgard.api.vo.SagaInstanceDetails;
import io.choerodon.asgard.api.vo.SagaInstanceFailureVO;
import io.choerodon.asgard.app.service.SagaInstanceService;
import io.choerodon.asgard.infra.dto.SagaInstanceDTO;
import io.choerodon.core.annotation.Permission;
import io.choerodon.core.enums.ResourceType;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Pageable;
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
@RequestMapping("/v1/sagas/organizations/{organization_id}/instances")
public class SagaInstanceOrgController {

    private SagaInstanceService sagaInstanceService;

    public SagaInstanceOrgController(SagaInstanceService sagaInstanceService) {
        this.sagaInstanceService = sagaInstanceService;
    }

    public void setSagaInstanceService(SagaInstanceService sagaInstanceService) {
        this.sagaInstanceService = sagaInstanceService;
    }

    @Permission(type = ResourceType.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @GetMapping
    @ApiOperation(value = "组织层查询事务实例列表")
    @ResponseBody
    @CustomPageRequest
    public ResponseEntity<PageInfo<SagaInstanceDetails>> pagingQuery(@ApiIgnore
                                                                     @SortDefault(value = "id", direction = Sort.Direction.ASC) Pageable pageable,
                                                                     @PathVariable("organization_id") long orgId,
                                                                     @RequestParam(required = false) String sagaCode,
                                                                     @RequestParam(required = false) String status,
                                                                     @RequestParam(required = false) String refType,
                                                                     @RequestParam(required = false) String refId,
                                                                     @RequestParam(required = false) String params) {
        return sagaInstanceService.pageQuery(pageable, sagaCode, status, refType, refId, params, ResourceLevel.ORGANIZATION.value(), orgId);
    }

    @Permission(type = ResourceType.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @GetMapping(value = "/{id}", produces = "application/json")
    @ApiOperation(value = "组织层查询某个事务实例运行详情")
    public ResponseEntity<String> query(@PathVariable("organization_id") long orgId,
                                        @PathVariable("id") Long id) {
        return sagaInstanceService.query(id);
    }

    @Permission(type = ResourceType.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @GetMapping(value = "/{id}/details", produces = "application/json")
    @ApiOperation(value = "组织层查询事务实例的具体信息")
    public ResponseEntity<SagaInstanceDetails> queryDetails(@PathVariable("organization_id") long orgId,
                                                            @PathVariable("id") Long id) {
        return new ResponseEntity<>(sagaInstanceService.queryDetails(id), HttpStatus.OK);
    }

    @Permission(type = ResourceType.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @GetMapping(value = "/statistics", produces = "application/json")
    @ApiOperation(value = "统计组织下各个事务实例状态下的实例个数")
    public ResponseEntity<Map> statistics(@PathVariable("organization_id") long orgId) {
        return new ResponseEntity<>(sagaInstanceService.statistics(ResourceLevel.ORGANIZATION.value(), orgId), HttpStatus.OK);
    }


    @Permission(type = ResourceType.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @GetMapping(value = "/statistics/failure")
    @ApiOperation(value = "统计组织下失败实例情况")
    public ResponseEntity<List<SagaInstanceFailureVO>> statisticsFailure(@PathVariable("organization_id") long orgId,
                                                                         @RequestParam("date") Integer date) {
        return new ResponseEntity<>(sagaInstanceService.statisticsFailure(ResourceLevel.ORGANIZATION.value(), orgId, date), HttpStatus.OK);
    }

    @Permission(type = ResourceType.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @GetMapping(value = "/statistics/failure/list")
    @CustomPageRequest
    @ApiOperation(value = "统计组织下失败实例情况详情")
    public ResponseEntity<PageInfo<SagaInstanceDTO>> statisticsFailureList(@ApiIgnore
                                                                       @SortDefault(value = "id", direction = Sort.Direction.DESC) Pageable pageable,
                                                                       @PathVariable("organization_id") long orgId,
                                                                       @RequestParam("date") Integer date) {
        return new ResponseEntity<>(sagaInstanceService.statisticsFailureList(ResourceLevel.ORGANIZATION.value(), orgId, date,pageable), HttpStatus.OK);
    }

}
