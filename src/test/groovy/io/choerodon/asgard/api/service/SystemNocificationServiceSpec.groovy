package io.choerodon.asgard.api.service

import io.choerodon.asgard.IntegrationTestConfiguration
import io.choerodon.asgard.api.dto.ScheduleTaskInstanceLogDTO
import io.choerodon.asgard.api.dto.SystemNotificationCreateDTO
import io.choerodon.asgard.api.dto.SystemNotificationDTO
import io.choerodon.asgard.api.service.impl.SystemNotificationServiceImpl
import io.choerodon.asgard.domain.QuartzTask
import io.choerodon.asgard.infra.mapper.QuartzTaskInstanceMapper
import io.choerodon.asgard.schedule.QuartzDefinition
import io.choerodon.core.domain.Page
import io.choerodon.core.iam.ResourceLevel
import io.choerodon.mybatis.pagehelper.domain.PageRequest
import io.choerodon.mybatis.pagehelper.domain.Sort
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class SystemNocificationServiceSpec extends Specification {


    private SystemNocificationService systemNocificationService
    private ScheduleMethodService scheduleMethodService = Mock(ScheduleMethodService)
    private ScheduleTaskService scheduleTaskService = Mock(ScheduleTaskService)
    private QuartzTaskInstanceMapper instanceMapper = Mock(QuartzTaskInstanceMapper)


    void setup() {
        systemNocificationService = new SystemNotificationServiceImpl(scheduleMethodService, scheduleTaskService, instanceMapper)
    }

    def "Create"() {
        given: "参数准备"
        def level = ResourceLevel.ORGANIZATION
        def dto = new SystemNotificationCreateDTO()
        dto.setContent("content")
        dto.setStartTime(new Date())
        def userId = 1L
        def sourceId = 1L
        def quartzTask = new QuartzTask()
        quartzTask.setId(1L)
        and: "mock"
        scheduleMethodService.getMethodIdByCode(_) >> { return 1L }
        scheduleTaskService.create(_, _, _) >> { return quartzTask }
        when: "方法调用"
        systemNocificationService.create(level, dto, userId, sourceId)
        then: "结果比较"
        noExceptionThrown()
    }

    def "GetDetailById"() {
        given: "参数准备"
        def level = ResourceLevel.ORGANIZATION
        def taskId = 1L
        def sourceId = 1L

        def quartzTask = new QuartzTask()
        quartzTask.setExecuteParams("{\"content\":\"系统公告测试kkkk\"}")
        quartzTask.setStartTime(new Date())

        def list = new ArrayList<ScheduleTaskInstanceLogDTO>()
        def logdto1 = new ScheduleTaskInstanceLogDTO()
        logdto1.setStatus(QuartzDefinition.InstanceStatus.COMPLETED.name())
        list.add(logdto1)
        and: "mock"
        scheduleTaskService.getQuartzTask(_, _, _) >> { return quartzTask }
        instanceMapper.selectByTaskId(taskId, _, _, _, _, _) >> { return list }
        when: "方法调用"
        systemNocificationService.getDetailById(level, taskId, sourceId)
        then: "结果比对"
        noExceptionThrown()
    }

    def "PagingAll"() {
        given: "参数准备"
        def status = null
        def content = "content"
        def params = null
        def level = ResourceLevel.SITE
        def sourceId = 0L

        and: "构造pageRequest"
        def order = new Sort.Order("start_time")
        def pageRequest = new PageRequest(1, 2, new Sort(order))

        and: "mock"
        scheduleTaskService.pagingAllNotification(_, _, _, _, _, _) >> {
            def list = new ArrayList<SystemNotificationDTO>()
            def entity1 = new SystemNotificationDTO(1, "{\"content\":\"系统公告测试kkkk\"}",
                    new Date(), SystemNotificationDTO.NotificationStatus.COMPLETED.value())
            list.add(entity1)
            def page = new Page()
            page.setContent(list)
            return page
        }

        when: "方法调用"
        systemNocificationService.pagingAll(pageRequest, status, content, params, level, sourceId)
        then: "结果分析"
        noExceptionThrown()
    }
}