package io.choerodon.asgard.infra.utils

import io.choerodon.asgard.IntegrationTestConfiguration
import io.choerodon.asgard.domain.QuartzTask
import io.choerodon.asgard.domain.QuartzTaskInstance
import io.choerodon.core.exception.CommonException
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class TriggerUtilsSpec extends Specification {
    def "GetNextFireTime"() {
        when: "方法调用"
        TriggerUtils.getNextFireTime(task, instance)
        then: "结果分析"
        noExceptionThrown()
        where: "分支覆盖"
        task                                                                                                          | instance
        new QuartzTask(triggerType: "cron-trigger", cronExpression: "* * 1 * * ?", endTime: null)                     | new QuartzTaskInstance(plannedStartTime: new Date())
        new QuartzTask(triggerType: "cron-trigger", cronExpression: "* * 1 * * ? ?", endTime: null)                   | new QuartzTaskInstance(plannedStartTime: new Date())
        new QuartzTask(triggerType: "cron-trigger", cronExpression: "* * 1 * * ?", endTime: new Date())               | new QuartzTaskInstance(plannedStartTime: new Date())
        new QuartzTask(triggerType: "simple-trigger", simpleRepeatInterval: 10L, simpleRepeatIntervalUnit: "SECONDS") | new QuartzTaskInstance(plannedStartTime: new Date())
    }

    def "GetRecentThree"() {
        given: "参数准备"
        def cron = "* * 1 * * ?"
        def errorCron = "error"

        when: "cron解析错误"
        TriggerUtils.getRecentThree(errorCron)
        then: "抛出异常"
        def e = thrown(CommonException)
        e.message == "error.cron.parse"

        when: "根据cron表达式获取最近三次执行时间"
        def three = TriggerUtils.getRecentThree(cron)
        then: "抛出异常"
        noExceptionThrown()
        three.size() == 3
    }

    def "GetStartTime"() {
        given: "参数准备"
        def cron = "* * 1 * * ?"
        def errorCron = "error"

        when: "cron解析错误"
        TriggerUtils.getStartTime(errorCron)
        then: "抛出异常"
        def e = thrown(CommonException)
        e.message == "error.cron.parse"

        when: "根据cron表达式获取下次执行时间"
        TriggerUtils.getStartTime(cron)
        then: "抛出异常"
        noExceptionThrown()
    }

}
