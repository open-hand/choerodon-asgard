package io.choerodon.asgard.api.controller.v1;

import io.choerodon.asgard.api.vo.*;
import io.choerodon.asgard.app.service.SagaInstanceService;
import io.choerodon.asgard.infra.dto.SagaInstanceDTO;
import io.choerodon.asgard.infra.utils.KeyDecryptHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.FeignException;
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
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/v1/sagas/instances")
public class SagaInstanceController {

    private static final String ERROR_INVALID_DTO = "error.startSaga.invalidDTO";

    private SagaInstanceService sagaInstanceService;

    public SagaInstanceController(SagaInstanceService sagaInstanceService) {
        this.sagaInstanceService = sagaInstanceService;
    }

    public void setSagaInstanceService(SagaInstanceService sagaInstanceService) {
        this.sagaInstanceService = sagaInstanceService;
    }

    /**
     * 内部接口。生产者端通过feign调用该接口
     * 开始执行一个saga
     */
    @PostMapping("/{code:.*}")
    @ApiOperation(value = "内部接口。开始一个saga")
    @Permission(permissionWithin = true)
    @ResponseBody
    public ResponseEntity<SagaInstance> start(@PathVariable("code") String code,
                                              @RequestBody StartInstance dto) {
        dto.setSagaCode(code);
        if (dto.getRefId() == null || dto.getRefType() == null) {
            throw new FeignException(ERROR_INVALID_DTO);
        }
        return sagaInstanceService.start(dto);
    }

    /**
     * 内部接口。预创建一个SAGA
     */
    @PostMapping
    @ApiOperation(value = "内部接口。预创建一个saga")
    @Permission(permissionWithin = true)
    @ResponseBody
    public ResponseEntity<SagaInstance> preCreate(@RequestBody StartInstance dto) {
        if (dto.getUuid() == null || StringUtils.isEmpty(dto.getSagaCode()) || StringUtils.isEmpty(dto.getService())) {
            throw new FeignException(ERROR_INVALID_DTO);
        }
        return sagaInstanceService.preCreate(dto);
    }

    @PostMapping("{uuid}/confirm")
    @ApiOperation(value = "内部接口。确认创建saga")
    @Permission(permissionWithin = true)
    @ResponseBody
    public void confirm(@PathVariable("uuid") String uuid, @RequestBody StartInstance dto) {
        if (dto.getRefType() == null || dto.getRefId() == null || dto.getInput() == null) {
            throw new FeignException(ERROR_INVALID_DTO);
        }
        sagaInstanceService.confirm(uuid, dto.getInput(), dto.getRefType(), dto.getRefId());
    }

    @PutMapping("{uuid}/cancel")
    @ApiOperation(value = "内部接口。取消创建saga")
    @Permission(permissionWithin = true)
    @ResponseBody
    public void cancel(@PathVariable("uuid") String uuid) {
        sagaInstanceService.cancel(uuid);
    }

    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @GetMapping
    @ApiOperation(value = "平台层查询事务实例列表")
    @ResponseBody
    @CustomPageRequest
    public ResponseEntity<Page<SagaInstanceDetails>> pagingQuery(@ApiIgnore
                                                                 @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest,
                                                                 @RequestParam(required = false) String sagaCode,
                                                                 @RequestParam(required = false) String status,
                                                                 @RequestParam(required = false) String refType,
                                                                 @RequestParam(required = false) String refId,
                                                                 @RequestParam(required = false) String params) {
        return sagaInstanceService.pageQuery(pageRequest, KeyDecryptHelper.decryptSagaCode(sagaCode), status, refType, refId, params, null, null);
    }

    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @GetMapping(value = "/{id}", produces = "application/json")
    @ApiOperation(value = "平台层查询某个事务实例运行详情")
    public ResponseEntity<SagaWithTaskInstance> query(@Encrypt @PathVariable("id") Long id) {
        return new ResponseEntity<>(sagaInstanceService.query(id), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @GetMapping(value = "/{id}/details", produces = "application/json")
    @ApiOperation(value = "平台层查询事务实例的具体信息")
    public ResponseEntity<SagaInstanceDetails> queryDetails(@Encrypt @PathVariable("id") Long id) {
        return new ResponseEntity<>(sagaInstanceService.queryDetails(id), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @GetMapping(value = "/statistics", produces = "application/json")
    @ApiOperation(value = "统计全平台各个事务实例状态下的实例个数")
    public ResponseEntity<Map<String, Integer>> statistics() {
        return new ResponseEntity<>(sagaInstanceService.statistics(null, null), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @GetMapping(value = "/statistics/failure")
    @ApiOperation(value = "统计平台下失败实例情况")
    public ResponseEntity<List<SagaInstanceFailureVO>> statisticsFailure(@RequestParam("date") Integer date) {
        return new ResponseEntity<>(sagaInstanceService.statisticsFailure(ResourceLevel.SITE.value(), null, date), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @GetMapping(value = "/statistics/failure/list")
    @CustomPageRequest
    @ApiOperation(value = "统计平台下失败实例情况详情")
    public ResponseEntity<Page<SagaInstanceDTO>> statisticsFailureList(@ApiIgnore
                                                                       @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest PageRequest,
                                                                       @RequestParam("date") Integer date) {
        return new ResponseEntity<>(sagaInstanceService.statisticsFailureList(ResourceLevel.SITE.value(), null, date, PageRequest), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_DEVELOPER, InitRoleCode.SITE_ADMINISTRATOR})
    @ApiOperation("根据日期查询事务失败的次数")
    @GetMapping("/failed/count")
    public ResponseEntity<Map<String, Object>> queryFailedByDate(@RequestParam(value = "begin_date")
                                                                 @ApiParam(value = "日期格式yyyy-MM-dd", required = true) String beginDate,
                                                                 @RequestParam(value = "end_date")
                                                                 @ApiParam(value = "日期格式yyyy-MM-dd", required = true) String endDate) {
        return new ResponseEntity<>(sagaInstanceService.queryFailedByDate(beginDate, endDate), HttpStatus.OK);
    }

}
