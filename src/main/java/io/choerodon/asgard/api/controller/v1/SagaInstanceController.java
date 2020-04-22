package io.choerodon.asgard.api.controller.v1;

import io.choerodon.asgard.api.dto.SagaInstanceDTO;
import io.choerodon.asgard.app.service.SagaInstanceC7nService;
import io.choerodon.asgard.api.vo.SagaInstanceFailureVO;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

/**
 * User: Mr.Wang
 * Date: 2020/4/22
 */
@Controller
@RequestMapping("choerodon/v1/sagas/instances")
public class SagaInstanceController {
    private static final String ERROR_INVALID_DTO = "error.startSaga.invalidDTO";

    private SagaInstanceC7nService sagaInstanceC7nService;

    public SagaInstanceController(SagaInstanceC7nService sagaInstanceC7nService) {
        this.sagaInstanceC7nService = sagaInstanceC7nService;
    }

    public void setSagaInstanceC7nService(SagaInstanceC7nService sagaInstanceC7nService) {
        this.sagaInstanceC7nService = sagaInstanceC7nService;
    }



    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @GetMapping(value = "/statistics/failure")
    @ApiOperation(value = "统计平台下失败实例情况")
    public ResponseEntity<List<SagaInstanceFailureVO>> statisticsFailure(@RequestParam("date") Integer date) {
        return new ResponseEntity<>(sagaInstanceC7nService.statisticsFailure(ResourceLevel.SITE.value(), null, date), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @GetMapping(value = "/statistics/failure/list")
    @CustomPageRequest
    @ApiOperation(value = "统计平台下失败实例情况详情")
    public ResponseEntity<Page<SagaInstanceDTO>> statisticsFailureList(@ApiIgnore
                                                                       @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest,
                                                                       @RequestParam("date") Integer date) {
        return new ResponseEntity<>(sagaInstanceC7nService.statisticsFailureList(ResourceLevel.SITE.value(), null, date, pageRequest), HttpStatus.OK);
    }


}
