package io.choerodon.asgard.app.service.impl

import io.choerodon.asgard.api.vo.QuartzTask
import io.choerodon.asgard.api.vo.Role

import io.choerodon.asgard.infra.dto.QuartzTaskDTO
import io.choerodon.asgard.infra.dto.QuartzTaskMemberDTO
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
        Role role = new Role(level: "site", name: "name", code: "code")
        Page<NoticeSendDTO.User> users = new Page<>()
        NoticeSendDTO.User user = new NoticeSendDTO.User()
        user.setId(1L)
        user.setEmail("123@qq.com")
        ArrayList<NoticeSendDTO.User> array = new ArrayList<>()
        array.add(user)
        users.setContent(array)
        QuartzTaskDTO quartzTask = new QuartzTaskDTO(level: "site")
        List<QuartzTaskMemberDTO> noticeMember = new ArrayList<>()
        noticeMember.add(new QuartzTaskMemberDTO(taskId: 1L, memberType: "user", memberId: 1L))
        noticeMember.add(new QuartzTaskMemberDTO(taskId: 1L, memberType: "role", memberId: 1L))
        String jobStatus = "启用"
        def response = new ResponseEntity<Page<NoticeSendDTO.User>>(users, HttpStatus.OK)
        def res = new ResponseEntity<Role>(role, HttpStatus.OK)

        when: "调用方法"
        noticeService.sendNotice(quartzTask, noticeMember, jobStatus)

        then: "校验结果"
        iamFeignClient.queryByCode(_) >> res
        iamFeignClient.pagingQueryUsersByRoleIdOnSiteLevel(_, _) >> response
        noExceptionThrown()

        when: "调用方法"
        quartzTask = new QuartzTaskDTO(level: "organization", sourceId: 1L)
        noticeService.sendNotice(quartzTask, noticeMember, jobStatus)

        then: "校验结果"
        iamFeignClient.queryByCode(_) >> res
        iamFeignClient.pagingQueryUsersByRoleIdOnOrganizationLevel(_, _, _) >> response
        noExceptionThrown()

        when: "调用方法"
        quartzTask = new QuartzTaskDTO(level: "project", sourceId: 1L)
        noticeService.sendNotice(quartzTask, noticeMember, jobStatus)

        then: "校验结果"
        iamFeignClient.queryByCode(_) >> res
        iamFeignClient.pagingQueryUsersByRoleIdOnProjectLevel(_, _, _) >> response
        noExceptionThrown()
    }
}
