package io.choerodon.asgard.api.service

import io.choerodon.asgard.IntegrationTestConfiguration
import io.choerodon.asgard.api.service.impl.QuartzRealJobServiceImpl
import io.choerodon.asgard.domain.QuartzMethod
import io.choerodon.asgard.domain.QuartzTask
import io.choerodon.asgard.domain.QuartzTaskInstance
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

    void setup() {
        quartzRealJobService = new QuartzRealJobServiceImpl(mockTaskMapper,
                mockQuartzTaskInstanceMapper,
                mockQuartzMethodMapper,
                mockScheduleTaskService,
                mockScheduleTaskInstanceService)
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
        lastInstance                                                                                      | date       | task                                                                                                                                                                              | db                                 | num
        null                                                                                              | null       | new QuartzTask(executeStrategy: "stop")                                                                                                                                                                              | null                               | 0
        new QuartzTaskInstance(id: 1L, status: "RUNNING")                                                 | null       | new QuartzTask(executeStrategy: "stop")                                                                                                                                                                              | null                               | 0
        null                                                                                              | new Date() | new QuartzTask(status: "DISABLE", executeStrategy: "stop")                                                                                                                        | null                               | 0
        null                                                                                              | new Date() | new QuartzTask(status: "ENABLE", executeMethod: "method", executeStrategy: "stop")                                                                                                | null                               | 0
        null                                                                                              | new Date() | new QuartzTask(status: "ENABLE", executeMethod: "method", executeStrategy: "stop", name: "name", executeParams: "", triggerType: "cron-trigger", cronExpression: "0/1 * * * * ?") | new QuartzMethod(maxRetryCount: 1) | 0
        new QuartzTaskInstance(status: "COMPLETED", actualStartTime: new Date(), executeResult: "result") | new Date() | new QuartzTask(status: "ENABLE", executeMethod: "method", executeStrategy: "stop", name: "name", executeParams: "", triggerType: "cron-trigger", cronExpression: "0/1 * * * * ?") | new QuartzMethod(maxRetryCount: 1) | 1

    }
}
