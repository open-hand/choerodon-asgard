package io.choerodon.asgard.api.controller.v1;

import com.github.pagehelper.PageInfo;
import io.choerodon.asgard.api.vo.SagaInstance;
import io.choerodon.asgard.api.vo.SagaInstanceDetails;
import io.choerodon.asgard.api.vo.StartInstance;
import io.choerodon.asgard.app.service.SagaInstanceService;
import io.choerodon.core.annotation.Permission;
import io.choerodon.core.enums.ResourceType;
import io.choerodon.core.exception.FeignException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

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

    @Permission(type = ResourceType.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @GetMapping
    @ApiOperation(value = "平台层查询事务实例列表")
    @ResponseBody
    @CustomPageRequest
    public ResponseEntity<PageInfo<SagaInstanceDetails>> pagingQuery(@ApiIgnore
                                                                     @SortDefault(value = "id", direction = Sort.Direction.DESC) Pageable pageable,
                                                                     @RequestParam(required = false) String sagaCode,
                                                                     @RequestParam(required = false) String status,
                                                                     @RequestParam(required = false) String refType,
                                                                     @RequestParam(required = false) String refId,
                                                                     @RequestParam(required = false) String params) {
        return sagaInstanceService.pageQuery(pageable, sagaCode, status, refType, refId, params, null, null);
    }

    @Permission(type = ResourceType.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @GetMapping(value = "/{id}", produces = "application/json")
    @ApiOperation(value = "平台层查询某个事务实例运行详情")
    public ResponseEntity<String> query(@PathVariable("id") Long id) {
        return sagaInstanceService.query(id);
    }

    @Permission(type = ResourceType.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @GetMapping(value = "/{id}/details", produces = "application/json")
    @ApiOperation(value = "平台层查询事务实例的具体信息")
    public ResponseEntity<SagaInstanceDetails> queryDetails(@PathVariable("id") Long id) {
        return new ResponseEntity<>(sagaInstanceService.queryDetails(id), HttpStatus.OK);
    }

    @Permission(type = ResourceType.SITE, roles = {InitRoleCode.SITE_DEVELOPER})
    @GetMapping(value = "/statistics", produces = "application/json")
    @ApiOperation(value = "统计全平台各个事务实例状态下的实例个数")
    public ResponseEntity<Map> statistics() {
        return new ResponseEntity<>(sagaInstanceService.statistics(null, null), HttpStatus.OK);
    }

    @Permission(type = ResourceType.SITE, roles = {InitRoleCode.SITE_DEVELOPER, InitRoleCode.SITE_ADMINISTRATOR})
    @ApiOperation("根据日期查询事务失败的次数")
    @GetMapping("/failed/count")
    public ResponseEntity<Map<String, Object>> queryFailedByDate(@RequestParam(value = "begin_date")
                                                                 @ApiParam(value = "日期格式yyyy-MM-dd", required = true) String beginDate,
                                                                 @RequestParam(value = "end_date")
                                                                 @ApiParam(value = "日期格式yyyy-MM-dd", required = true) String endDate) {
        return new ResponseEntity<>(sagaInstanceService.queryFailedByDate(beginDate, endDate), HttpStatus.OK);
    }


}
