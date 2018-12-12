package io.choerodon.asgard.api.controller.v1;

import javax.validation.Valid;

import io.choerodon.asgard.api.dto.SystemNotificationUpdateDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.asgard.api.dto.SystemNotificationCreateDTO;
import io.choerodon.asgard.api.dto.SystemNotificationDTO;
import io.choerodon.asgard.api.service.ScheduleTaskService;
import io.choerodon.asgard.api.service.SystemNocificationService;
import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

@RestController
@RequestMapping("/v1/system_notice")
@Api("平台/组织层公告接口")
public class SystemNotificationController {
    private SystemNocificationService systemNocificationService;
    private ScheduleTaskService scheduleTaskService;

    public SystemNotificationController(SystemNocificationService systemNocificationService, ScheduleTaskService scheduleTaskService) {
        this.systemNocificationService = systemNocificationService;
        this.scheduleTaskService = scheduleTaskService;
    }

    public void setSystemNocificationService(SystemNocificationService systemNocificationService) {
        this.systemNocificationService = systemNocificationService;
    }

    public void setScheduleTaskService(ScheduleTaskService scheduleTaskService) {
        this.scheduleTaskService = scheduleTaskService;
    }

    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_ADMINISTRATOR})
    @ApiOperation(value = "创建系统公告")
    @PostMapping("/create")
    public ResponseEntity<SystemNotificationDTO> createNotificationOnSite(@RequestBody @Valid SystemNotificationCreateDTO dto) {
        Long userId = DetailsHelper.getUserDetails().getUserId();
        return new ResponseEntity<>(systemNocificationService.create(ResourceLevel.SITE, dto, userId, 0L), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_ADMINISTRATOR})
    @ApiOperation(value = "更新系统公告")
    @PutMapping("/update")
    public ResponseEntity<SystemNotificationDTO> updateNotificationOnSite(@RequestBody @Valid SystemNotificationUpdateDTO dto) {
        return new ResponseEntity<>(systemNocificationService.update(dto, ResourceLevel.SITE, 0L), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_ADMINISTRATOR})
    @ApiOperation(value = "平台层删除公告")
    @DeleteMapping("/delete")
    public void deleteSiteNotification(@RequestParam(name = "taskId") Long taskId) {
        scheduleTaskService.delete(taskId, ResourceLevel.SITE.value(), 0L);
    }

    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_ADMINISTRATOR})
    @GetMapping("/detail/{id}")
    @ApiOperation(value = "全局层查看公告详情")
    public ResponseEntity<SystemNotificationDTO> getSiteNotificationDetails(@PathVariable long id) {
        return new ResponseEntity<>(systemNocificationService.getDetailById(ResourceLevel.SITE, id, 0L), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.SITE, roles = {InitRoleCode.SITE_ADMINISTRATOR})
    @GetMapping("/all")
    @ApiOperation(value = "全局层分页查询系统公告")
    @CustomPageRequest
    @ResponseBody
    public ResponseEntity<Page<SystemNotificationDTO>> pagingQuerySiteNotification(@RequestParam(value = "status", required = false) String status,
                                                                                   @RequestParam(name = "content", required = false) String content,
                                                                                   @RequestParam(name = "params", required = false) String params,
                                                                                   @ApiIgnore
                                                                                   @SortDefault(value = "start_time", direction = Sort.Direction.DESC) PageRequest pageRequest) {
        return new ResponseEntity<>(systemNocificationService.pagingAll(pageRequest, status, content, params, ResourceLevel.SITE, 0L), HttpStatus.OK);
    }

//    @Permission(level = ResourceLevel.ORGANIZATION)
//    @ApiOperation(value = "创建组织公告")
//    @PostMapping("/organization/{organization_id}/create")
//    public ResponseEntity<SystemNotificationDTO> createNotificationOnOrg(@RequestBody @Valid SystemNotificationCreateDTO dto,
//                                                                         @PathVariable("organization_id") long orgId) {
//        Long userId = DetailsHelper.getUserDetails().getUserId();
//        return new ResponseEntity<>(systemNocificationService.create(ResourceLevel.ORGANIZATION, dto, userId, orgId), HttpStatus.OK);
//    }

//    @Permission(level = ResourceLevel.ORGANIZATION)
//    @ApiOperation(value = "组织层删除公告")
//    @DeleteMapping("/organization/{organization_id}/delete")
//    public void deleteOrgNotification(@PathVariable("organization_id") long orgId,
//                                      @RequestParam(name = "taskId") Long taskId) {
//        scheduleTaskService.delete(taskId, ResourceLevel.ORGANIZATION.value(), orgId);
//    }
//
//    @Permission(level = ResourceLevel.ORGANIZATION)
//    @GetMapping("/organization/{organization_id}/detail/{id}")
//    @ApiOperation(value = "组织层查看公告详情")
//    public ResponseEntity<SystemNotificationDTO> getOrgNotificationDetails(@PathVariable("organization_id") long orgId,
//                                                                           @PathVariable long id) {
//        return new ResponseEntity<>(systemNocificationService.getDetailById(ResourceLevel.ORGANIZATION, id, orgId), HttpStatus.OK);
//    }

//    @Permission(level = ResourceLevel.ORGANIZATION)
//    @GetMapping("/organization/{organization_id}/all")
//    @ApiOperation(value = "组织层分页查询系统公告")
//    @CustomPageRequest
//    @ResponseBody
//    public ResponseEntity<Page<SystemNotificationDTO>> pagingQueryOrgNotificaton(@PathVariable("organization_id") long orgId,
//                                                                                 @RequestParam(value = "status", required = false) String status,
//                                                                                 @RequestParam(name = "content", required = false) String content,
//                                                                                 @RequestParam(name = "params", required = false) String params,
//                                                                                 @ApiIgnore
//                                                                                 @SortDefault(value = "start_time",direction = Sort.Direction.DESC) PageRequest pageRequest) {
//        return new ResponseEntity<>(systemNocificationService.pagingAll(pageRequest, status, content, params, ResourceLevel.ORGANIZATION, orgId), HttpStatus.OK);
//    }
}
