package io.choerodon.asgard.api.controller.v1;

import io.choerodon.asgard.api.dto.SagaInstanceDTO;
import io.choerodon.asgard.api.vo.SagaInstanceFailureVO;
import io.choerodon.asgard.app.service.SagaInstanceC7nService;
import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

/**
 * User: Mr.Wang
 * Date: 2020/4/22
 */
@Controller
@RequestMapping("choerodon/v1/sagas/organizations/{organization_id}/instances")
public class SagaInstanceOrgController {
    private SagaInstanceC7nService sagaInstanceC7nService;

    public SagaInstanceOrgController(SagaInstanceC7nService sagaInstanceC7nService) {
        this.sagaInstanceC7nService = sagaInstanceC7nService;
    }

    public void setSagaInstanceService(SagaInstanceC7nService sagaInstanceC7nService) {
        this.sagaInstanceC7nService = sagaInstanceC7nService;
    }


    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @GetMapping(value = "/statistics/failure")
    @ApiOperation(value = "统计组织下失败实例情况")
    public ResponseEntity<List<SagaInstanceFailureVO>> statisticsFailure(@PathVariable("organization_id") long orgId,
                                                                         @RequestParam("date") Integer date) {
        return new ResponseEntity<>(sagaInstanceC7nService.statisticsFailure(ResourceLevel.ORGANIZATION.value(), orgId, date), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @GetMapping(value = "/statistics/failure/list")
    @CustomPageRequest
    @ApiOperation(value = "统计组织下失败实例情况详情")
    public ResponseEntity<Page<SagaInstanceDTO>> statisticsFailureList(@ApiIgnore
                                                                       @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest,
                                                                       @PathVariable("organization_id") long orgId,
                                                                       @RequestParam("date") Integer date) {
        return new ResponseEntity<>(sagaInstanceC7nService.statisticsFailureList(ResourceLevel.ORGANIZATION.value(), orgId, date, pageRequest), HttpStatus.OK);
    }

}
