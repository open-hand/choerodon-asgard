package io.choerodon.asgard.app.service

import io.choerodon.asgard.IntegrationTestConfiguration

import io.choerodon.asgard.infra.dto.QuartzTaskDTO
import io.choerodon.core.exception.CommonException
import org.quartz.Scheduler
import org.quartz.SchedulerException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class QuartzJobServiceSpec extends Specification {

    @Autowired
    QuartzJobServiceImpl quartzJobService

    private Scheduler mockScheduler = Mock(Scheduler)

    void setup() {
        quartzJobService.setScheduler(mockScheduler)
    }

    def "AddJob"() {
        given: "参数准备"
        def cronTask = new QuartzTaskDTO(id: 1L, name: "name", startTime: new Date(), endTime: new Date(), triggerType: "cron-trigger", cronExpression: "0/1 * * * * ?")
        and: "mock"
        mockScheduler.isShutdown() >> { return false }
        mockScheduler.isStarted() >> { return false }

        when: "方法调用"
        quartzJobService.addJob(cronTask)
        then: "结果分析"
        noExceptionThrown()
    }

    def "AddJob[Exception]"() {
        given: "参数准备"
        def simpleTask = new QuartzTaskDTO(id: 1L, name: "name", startTime: null, endTime: new Date(), triggerType: "simple-trigger", simpleRepeatCount: 5, simpleRepeatInterval: 10L, simpleRepeatIntervalUnit: "SECONDS")
        and: "mock"
        mockScheduler.isShutdown() >> { throw new SchedulerException("msg") }
        mockScheduler.isStarted() >> { return false }

        when: "方法调用"
        quartzJobService.addJob(simpleTask)
        then: "结果分析"
        def e = thrown(CommonException)
        e.message == "error.quartzJobService.addJob"
    }

    def "ResumeJob"() {
        given: "参数准备"
        def id = 100L
        and: "mock"
        mockScheduler.resumeJob(_) >> { throw new SchedulerException("") }
        when: "方法调用抛出异常"
        quartzJobService.resumeJob(id)
        then: "异常获取"
        def e = thrown(CommonException)
        e.message == "error.scheduleTask.enableTaskFailed"
    }

    def "PauseJob"() {
        given: "参数准备"
        def id = 100L
        and: "mock"
        mockScheduler.pauseJob(_) >> { throw new SchedulerException("") }
        when: "方法调用抛出异常"
        quartzJobService.pauseJob(id)
        then: "异常获取"
        def e = thrown(CommonException)
        e.message == "error.scheduleTask.disableTaskFailed"
    }

    def "RemoveJob"() {
        given: "参数准备"
        def id = 100L

        and: "mock"
        mockScheduler.deleteJob(_) >> { throw new SchedulerException("") }

        when: "方法调用抛出异常"
        quartzJobService.removeJob(id)
        then: "异常获取"
        def e = thrown(CommonException)
        e.message == "error.scheduleTask.deleteTaskFailed"
    }

}
