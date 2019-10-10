package io.choerodon.asgard.app.service

import io.choerodon.asgard.IntegrationTestConfiguration
import io.choerodon.asgard.api.vo.QuartzTask
import io.choerodon.asgard.app.eventhandler.SagaInstanceEventPublisher

import io.choerodon.asgard.infra.dto.QuartzMethodDTO
import io.choerodon.asgard.infra.dto.QuartzTaskDTO
import io.choerodon.asgard.infra.dto.QuartzTaskInstanceDTO
import io.choerodon.asgard.infra.mapper.QuartzMethodMapper
import io.choerodon.asgard.infra.mapper.QuartzTaskInstanceMapper
import io.choerodon.asgard.infra.mapper.QuartzTaskMapper
import org.quartz.CronExpression
import org.quartz.JobExecutionContext
import org.quartz.impl.JobDetailImpl
import org.quartz.impl.JobExecutionContextImpl
import org.quartz.impl.triggers.CronTriggerImpl
import org.quartz.spi.TriggerFiredBundle
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class QuartzRealJobServiceSpec extends Specification {

    private QuartzRealJobService quartzRealJobService
    private QuartzTaskMapper mockTaskMapper = Mock(QuartzTaskMapper)
    private QuartzTaskInstanceMapper mockQuartzTaskInstanceMapper = Mock(QuartzTaskInstanceMapper)
    private QuartzMethodMapper mockQuartzMethodMapper = Mock(QuartzMethodMapper)
    private ScheduleTaskService mockScheduleTaskService = Mock(ScheduleTaskService)
    private ScheduleTaskInstanceService mockScheduleTaskInstanceService = Mock(ScheduleTaskInstanceService)
    private SagaInstanceEventPublisher mockSagaInstanceEventPublisher = Mock(SagaInstanceEventPublisher)

    void setup() {
        quartzRealJobService = new QuartzRealJobServiceImpl(mockTaskMapper,
                mockQuartzTaskInstanceMapper,
                mockQuartzMethodMapper,
                mockScheduleTaskService,
                mockScheduleTaskInstanceService,
                mockSagaInstanceEventPublisher)
    }

    def "TriggerEvent"() {
        given: "参数准备"
        def taskId = 1L
        and: "mock"
        mockQuartzTaskInstanceMapper.selectLastInstance(taskId) >> { return lastInstance }
        mockTaskMapper.selectByPrimaryKey(taskId) >> { return task }
        mockQuartzMethodMapper.selectOne(_) >> { return db }
        mockQuartzTaskInstanceMapper.insert(_) >> { return num }
        JobExecutionContext jobExecutionContext = new JobExecutionContextImpl(null, new TriggerFiredBundle(new JobDetailImpl(), new CronTriggerImpl(cronEx: new CronExpression("0/1 * * * * ?")), null, true, null, null, null, date), null)
        when: "方法调用"
        quartzRealJobService.triggerEvent(taskId, jobExecutionContext)
        then: "无异常抛出"
        noExceptionThrown()
        where: "条件覆盖"
        lastInstance                                         | date       | task                                                       | db   | num
        null                                                 | null       | new QuartzTaskDTO(executeStrategy: "stop")                 | null | 0
        new QuartzTaskInstanceDTO(id: 1L, status: "RUNNING") | null       | new QuartzTaskDTO(executeStrategy: "stop")                    | null | 0
        null                                                 | new Date() | new QuartzTaskDTO(status: "DISABLE", executeStrategy: "stop") | null | 0
        null                                                 | new Date() | new QuartzTaskDTO(status: "ENABLE", executeMethod: "method", executeStrategy: "stop") | null                                                                                                                                                                              | 0
        null                                                                                              | new Date() | new QuartzTaskDTO(status: "ENABLE", executeMethod: "method", executeStrategy: "stop", name: "name", executeParams: "", triggerType: "cron-trigger", cronExpression: "0/1 * * * * ?") | new QuartzMethodDTO(maxRetryCount: 1) | 0
        new QuartzTaskInstanceDTO(status: "COMPLETED", actualStartTime: new Date(), executeResult: "result") | new Date() | new QuartzTaskDTO(status: "ENABLE", executeMethod: "method", executeStrategy: "stop", name: "name", executeParams: "", triggerType: "cron-trigger", cronExpression: "0/1 * * * * ?") | new QuartzMethodDTO(maxRetryCount: 1) | 1

    }
}
