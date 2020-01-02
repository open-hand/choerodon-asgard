package io.choerodon.asgard.api.controller.v1;

import java.util.Map;

import com.github.pagehelper.PageInfo;
import io.choerodon.asgard.app.service.SagaInstanceService;
import io.choerodon.core.annotation.Permission;
import io.choerodon.core.enums.ResourceType;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import io.choerodon.asgard.api.vo.SagaInstanceDetails;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import springfox.documentation.annotations.ApiIgnore;

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

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_ADMINISTRATOR})
    @GetMapping
    @ApiOperation(value = "项目层查询事务实例列表")
    @ResponseBody
    @CustomPageRequest
    public ResponseEntity<PageInfo<SagaInstanceDetails>> pagingQuery(@PathVariable("project_id") long projectId,
                                                                     @RequestParam(required = false) String sagaCode,
                                                                     @RequestParam(required = false) String status,
                                                                     @RequestParam(required = false) String refType,
                                                                     @RequestParam(required = false) String refId,
                                                                     @RequestParam(required = false) String params,
                                                                     @ApiIgnore
                                                                     @SortDefault(value = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return sagaInstanceService.pageQuery(pageable, sagaCode, status, refType, refId, params, ResourceLevel.PROJECT.value(), projectId);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_ADMINISTRATOR})
    @GetMapping(value = "/{id}", produces = "application/json")
    @ApiOperation(value = "项目层查询某个事务实例运行详情")
    public ResponseEntity<String> query(@PathVariable("project_id") long projectId,
                                        @PathVariable("id") Long id) {
        return sagaInstanceService.query(id);
    }


    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_ADMINISTRATOR})
    @GetMapping(value = "/{id}/details", produces = "application/json")
    @ApiOperation(value = "项目层查询事务实例的具体信息")
    public ResponseEntity<SagaInstanceDetails> queryDetails(@PathVariable("project_id") long projectId,
                                                            @PathVariable("id") Long id) {
        return new ResponseEntity<>(sagaInstanceService.queryDetails(id), HttpStatus.OK);
    }


    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_ADMINISTRATOR})
    @GetMapping(value = "/statistics", produces = "application/json")
    @ApiOperation(value = "统计项目下各个事务实例状态下的实例个数")
    public ResponseEntity<Map> statistics(@PathVariable("project_id") long projectId) {
        return new ResponseEntity<>(sagaInstanceService.statistics(ResourceLevel.PROJECT.value(), projectId), HttpStatus.OK);
    }


}
