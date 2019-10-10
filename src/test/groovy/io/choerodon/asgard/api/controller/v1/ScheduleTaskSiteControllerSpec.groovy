package io.choerodon.asgard.api.controller.v1

import io.choerodon.asgard.IntegrationTestConfiguration
import io.choerodon.asgard.api.vo.QuartzTask
import io.choerodon.asgard.api.vo.ScheduleTask
import io.choerodon.asgard.api.vo.ScheduleTaskDetail

import io.choerodon.core.domain.Page
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import spock.lang.Specification

import java.text.SimpleDateFormat

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class ScheduleTaskSiteControllerSpec extends Specification {
    @Autowired
    TestRestTemplate restTemplate

    @Autowired
    private ScheduleTaskSiteController scheduleTaskController

    private ScheduleTaskService mockScheduleTaskService = Mock(ScheduleTaskService)

    void setup() {
        scheduleTaskController.setScheduleTaskService(mockScheduleTaskService)
    }

    def "Create"() {
        given: '参数准备'
        def scheduleTask = new ScheduleTask()
        scheduleTask.setMethodId(1L)
        scheduleTask.setName("name")
        scheduleTask.setTriggerType("invalid")
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        Date endTime = simpleDateFormat.parse("2018-08-08 08:08:00")
        scheduleTask.setEndTime(endTime)

        def inVaildScheduleTask = new ScheduleTask()
        inVaildScheduleTask.setName("")

        when: 'POST请求【创建定时任务】-参数不合法'
        def response = restTemplate.postForEntity("/v1/schedules/tasks", inVaildScheduleTask, QuartzTask)
        then: '状态码验证通过；验证方法参数不生效；验证异常正确抛出'
        response.statusCode.is2xxSuccessful()
        0 * mockScheduleTaskService.create(_, _, _)

        when: 'POST请求【创建定时任务】-结束时间为过去时间'
        response = restTemplate.postForEntity("/v1/schedules/tasks", scheduleTask, Object)
        then: '状态码验证通过；验证方法参数不生效；验证失败code返回正确'
        response.statusCode.is2xxSuccessful()
        response.body.get("code") == "error.scheduleTask.endTime.cantbefore.now"
        0 * mockScheduleTaskService.create(scheduleTask)

        when: 'POST请求【创建定时任务】-invalidTriggerType'
        scheduleTask.setEndTime(null)
        response = restTemplate.postForEntity("/v1/schedules/tasks", scheduleTask, Object)
        then: '状态码验证通过；验证方法参数不生效；验证失败code返回正确'
        response.statusCode.is2xxSuccessful()
        response.body.get("code") == "error.scheduleTask.invalidTriggerType"
        0 * mockScheduleTaskService.create(scheduleTask)

        when: 'POST请求【创建定时任务】-repeatCountOrRepeatIntervalNull'
        scheduleTask.setTriggerType("simple-trigger")
        scheduleTask.setSimpleRepeatCount(1)
        response = restTemplate.postForEntity("/v1/schedules/tasks", scheduleTask, Object)
        then: '状态码验证通过；验证方法参数不生效；验证失败code返回正确'
        response.statusCode.is2xxSuccessful()
        response.body.get("code") == "error.scheduleTask.repeatCountOrRepeatIntervalNull"
        0 * mockScheduleTaskService.create(scheduleTask, _, _)

        when: 'POST请求【创建定时任务】-repeatCountOrRepeatIntervalUnitNull'
        scheduleTask.setTriggerType("simple-trigger")
        scheduleTask.setSimpleRepeatInterval(10L)
        response = restTemplate.postForEntity("/v1/schedules/tasks", scheduleTask, Object)
        then: '状态码验证通过；验证方法参数不生效；验证失败code返回正确'
        response.statusCode.is2xxSuccessful()
        response.body.get("code") == "error.scheduleTask.repeatCountOrRepeatIntervalUnitNull"
        0 * mockScheduleTaskService.create(scheduleTask)

        when: 'POST请求【创建定时任务】-cronExpressionEmpty'
        scheduleTask.setTriggerType("cron-trigger")
        response = restTemplate.postForEntity("/v1/schedules/tasks", scheduleTask, Object)
        then: '状态码验证通过；验证方法参数不生效；验证失败code返回正确'
        response.statusCode.is2xxSuccessful()
        response.body.get("code") == "error.scheduleTask.cronExpressionEmpty"
        0 * mockScheduleTaskService.create(scheduleTask, _, _)

        when: 'POST请求【创建定时任务】-cronExpressionInvalid'
        scheduleTask.setTriggerType("cron-trigger")
        scheduleTask.setCronExpression("invalidCronExpression")
        response = restTemplate.postForEntity("/v1/schedules/tasks", scheduleTask, Object)
        then: '状态码验证通过；验证方法参数不生效；验证失败code返回正确'
        response.statusCode.is2xxSuccessful()
        response.body.get("code") == "error.scheduleTask.cronExpressionInvalid"
        0 * mockScheduleTaskService.create(scheduleTask, _, _)

        when: 'POST请求【创建定时任务】'
        scheduleTask.setTriggerType("cron-trigger")
        scheduleTask.setCronExpression("23,34 * * * * ?")
        response = restTemplate.postForEntity("/v1/schedules/tasks", scheduleTask, QuartzTask)
        then: '状态码验证通过；验证方法参数生效'
        response.statusCode.is2xxSuccessful()
        1 * mockScheduleTaskService.create(_, _, _)
    }

    def "Enable"() {
        given: '准备参数'
        def id = 1L
        def objectVersionNumber = 1L
        when: 'PUT请求【启用任务】'
        HttpEntity<Object> httpEntity = new HttpEntity<>()
        def response = restTemplate.exchange("/v1/schedules/tasks/{id}/enable?objectVersionNumber={objectVersionNumber}", HttpMethod.PUT, httpEntity, void, id, objectVersionNumber)
        then: '状态码验证通过；验证方法参数生效'
        response.statusCode.is2xxSuccessful()
        1 * mockScheduleTaskService.enable(_, _, _, _)
    }

    def "Disable"() {
        given: '准备参数'
        def id = 1L
        def objectVersionNumber = 1L
        when: 'PUT请求【停用任务】'
        HttpEntity<Object> httpEntity = new HttpEntity<>()
        def response = restTemplate.exchange("/v1/schedules/tasks/{id}/disable?objectVersionNumber={objectVersionNumber}", HttpMethod.PUT, httpEntity, void, id, objectVersionNumber)
        then: '状态码验证通过；验证方法参数生效'
        response.statusCode.is2xxSuccessful()
        1 * mockScheduleTaskService.disable(_, _, false)
    }

    def "Delete"() {
        given: "准备参数"
        def id = 1L
        when: 'DELETE请求【删除任务】'
        HttpEntity<Object> httpEntity = new HttpEntity<>()
        def response = restTemplate.exchange('/v1/schedules/tasks/{id}', HttpMethod.DELETE, httpEntity, void, id)
        then: '状态码验证通过；验证方法参数生效'
        response.statusCode.is2xxSuccessful()
        1 * mockScheduleTaskService.delete(_, _, _)
    }

    def "PagingQuery"() {
        given: "参数准备"
        def status = "RUNNING"
        def name = "name"
        def description = "description"
        def params = "params"
        def query = new HashMap<String, String>()
        query.put("status", status)
        query.put("name", name)
        query.put("description", description)
        query.put("params", params)

        when: 'GET请求【分页查询任务实例列表】'
        def response = restTemplate.getForEntity("/v1/schedules/tasks", Page, query)
        then: '状态码验证成功；参数验证合法'
        response.statusCode.is2xxSuccessful()
        1 * mockScheduleTaskService.pageQuery(_, _, _, _, _, _, _)
    }

    def "GetTaskDetail"() {
        given: "参数准备"
        def id = 1L
        when: 'GET请求【查看任务详情】'
        def response = restTemplate.getForEntity("/v1/schedules/tasks/{id}", ScheduleTaskDetail, id)
        then: '状态码验证成功；参数验证合法'
        response.statusCode.is2xxSuccessful()
        1 * mockScheduleTaskService.getTaskDetail(id, _, _)
    }

    def "Check"() {
        given: "参数准备"
        def name = "name"
        when: 'GET请求【查看任务详情】'
        def response = restTemplate.postForEntity("/v1/schedules/tasks/check", name, String)
        then: '状态码验证成功；参数验证合法'
        response.statusCode.is2xxSuccessful()
//        1 * mockScheduleTaskService.checkName(name, _, _)
    }

    def "Cron"() {
        given: "参数准备"
        def cron = "0/1 * * * * ?"
        when: 'GET请求【查看任务详情】'
        def response = restTemplate.postForEntity("/v1/schedules/tasks/cron", cron, List)
        then: '状态码验证成功；返回正确'
        response.statusCode.is2xxSuccessful()
        response.body.size() == 3
    }
}
