package io.choerodon.asgard.api.controller.v1

import com.github.pagehelper.PageInfo
import io.choerodon.asgard.IntegrationTestConfiguration

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class ScheduleTaskInstanceSiteControllerSpec extends Specification {
    @Autowired
    TestRestTemplate restTemplate
    @Autowired
    private ScheduleTaskInstanceSiteController scheduleTaskInstanceController

    private mockScheduleTaskInstanceService = Mock(ScheduleTaskInstanceService)

    void setup() {
        scheduleTaskInstanceController.setScheduleTaskInstanceService(mockScheduleTaskInstanceService)
    }

    def "PagingQuery"() {
        given: "参数准备"
        def status = "RUNNING"
        def taskName = "taskName"
        def exceptionMessage = "exceptionMessage"
        def params = "params"
        def query = new HashMap<String, String>()
        query.put("status", status)
        query.put("taskName", taskName)
        query.put("exceptionMessage", exceptionMessage)
        query.put("params", params)

        when: 'GET请求【分页查询任务实例列表】'
        def response = restTemplate.getForEntity("/v1/schedules/tasks/instances" +
                "?status={status}&taskName={taskName}&exceptionMessage={exceptionMessage}&params={params}", PageInfo, query)
        then: '状态码验证成功；参数验证合法'
        response.statusCode.is2xxSuccessful()
        1 * mockScheduleTaskInstanceService.pageQuery(_, _, status, taskName, exceptionMessage, params, _, _)
    }


    def "pagingQueryByTaskId"() {
        given: 'queryParams准备'
        def taskId = 1L
        def status = "status"
        def serviceInstanceId = "serviceInstanceId"
        def params = "params"
        def queryParams = new HashMap<String, String>()
        queryParams.put("status", status)
        queryParams.put("serviceInstanceId", serviceInstanceId)
        queryParams.put("params", params)

        when: '对接口【分页查询执行方法列表】发送GET请求'
        def entity = restTemplate.getForEntity("/v1/schedules/tasks/instances/{taskId}", PageInfo, taskId, queryParams, 1, 20)
        then: '状态码正确；方法参数调用成功'
        entity.statusCode.is2xxSuccessful()
        1 * mockScheduleTaskInstanceService.pagingQueryByTaskId(_, _, _, _, _, _, _, _)
    }
}
