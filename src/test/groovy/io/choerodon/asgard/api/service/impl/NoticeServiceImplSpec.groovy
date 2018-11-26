package io.choerodon.asgard.api.service.impl

import io.choerodon.asgard.api.dto.RoleDTO
import io.choerodon.asgard.api.service.NoticeService
import io.choerodon.asgard.domain.QuartzTask
import io.choerodon.asgard.domain.QuartzTaskMember
import io.choerodon.asgard.infra.feign.IamFeignClient
import io.choerodon.asgard.infra.feign.NotifyFeignClient
import io.choerodon.core.domain.Page
import io.choerodon.core.notify.NoticeSendDTO
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification

/**
 * @author dengyouquan
 * */

class NoticeServiceImplSpec extends Specification {
    private NotifyFeignClient notifyFeignClient = Mock(NotifyFeignClient)
    private IamFeignClient iamFeignClient = Mock(IamFeignClient)
    private NoticeService noticeService = new NoticeServiceImpl(notifyFeignClient, iamFeignClient)

    def "SendNotice"() {
        given: "构造请求参数"
        RoleDTO roleDTO = new RoleDTO(level: "site", name: "name", code: "code")
        Page<NoticeSendDTO.User> users = new Page<>()
        NoticeSendDTO.User user = new NoticeSendDTO.User()
        user.setId(1L)
        user.setEmail("123@qq.com")
        ArrayList<NoticeSendDTO.User> array = new ArrayList<>()
        array.add(user)
        users.setContent(array)
        QuartzTask quartzTask = new QuartzTask(level: "site")
        List<QuartzTaskMember> noticeMember = new ArrayList<>()
        noticeMember.add(new QuartzTaskMember(taskId: 1L, memberType: "user", memberId: 1L))
        noticeMember.add(new QuartzTaskMember(taskId: 1L, memberType: "role", memberId: 1L))
        String jobStatus = "启用"
        def response = new ResponseEntity<Page<NoticeSendDTO.User>>(users, HttpStatus.OK)
        def res = new ResponseEntity<RoleDTO>(roleDTO, HttpStatus.OK)

        when: "调用方法"
        noticeService.sendNotice(quartzTask, noticeMember, jobStatus)

        then: "校验结果"
        iamFeignClient.queryByCode(_) >> res
        iamFeignClient.pagingQueryUsersByRoleIdOnSiteLevel(_, _) >> response
        noExceptionThrown()

        when: "调用方法"
        quartzTask = new QuartzTask(level: "organization", sourceId: 1L)
        noticeService.sendNotice(quartzTask, noticeMember, jobStatus)

        then: "校验结果"
        iamFeignClient.queryByCode(_) >> res
        iamFeignClient.pagingQueryUsersByRoleIdOnOrganizationLevel(_, _, _) >> response
        noExceptionThrown()

        when: "调用方法"
        quartzTask = new QuartzTask(level: "project", sourceId: 1L)
        noticeService.sendNotice(quartzTask, noticeMember, jobStatus)

        then: "校验结果"
        iamFeignClient.queryByCode(_) >> res
        iamFeignClient.pagingQueryUsersByRoleIdOnProjectLevel(_, _, _) >> response
        noExceptionThrown()
    }
}
