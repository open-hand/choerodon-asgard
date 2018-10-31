package io.choerodon.asgard.api.controller.v1

import io.choerodon.asgard.IntegrationTestConfiguration
import io.choerodon.asgard.UpdateTaskInstanceStatusDTO
import io.choerodon.asgard.api.service.ScheduleTaskInstanceService
import io.choerodon.core.domain.Page
import io.choerodon.mybatis.pagehelper.domain.PageRequest
import io.choerodon.mybatis.pagehelper.domain.Sort
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
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
                "?status={status}&taskName={taskName}&exceptionMessage={exceptionMessage}&params={params}", Page, query)
        then: '状态码验证成功；参数验证合法'
        response.statusCode.is2xxSuccessful()
        1 * mockScheduleTaskInstanceService.pageQuery(_, status, taskName, exceptionMessage, params, _, _)
    }

    def "PollBatch"() {
        given: '参数准备'
        def methods = new HashSet<String>()
        methods.add("methods1")
        def instance = "test-service:1000:20180911"
        when: 'POST请求【内部接口。拉取指定method的定时任务消息列表】'
        def response = restTemplate.postForEntity("/v1/schedules/tasks/instances/poll/batch?instance={instance}", methods, Set, instance)
        then: '状态码验证成功；参数验证合法'
        response.statusCode.is2xxSuccessful()
        1 * mockScheduleTaskInstanceService.pollBatch(_, _)
    }

    def "updateStatus"() {
        given: '参数准备'
        def id = 1L
        def validStatusDTO = new UpdateTaskInstanceStatusDTO()
        validStatusDTO.setStatus("RUNNNING")
        def invalidStatusDTO = new UpdateTaskInstanceStatusDTO()
        when: 'PUT请求【内部接口。更新任务的执行状态】'
        HttpEntity<UpdateTaskInstanceStatusDTO> requestEntity1 = new HttpEntity<>(validStatusDTO)
        def response1 = restTemplate.exchange("/v1/schedules/tasks/instances/{id}/status", HttpMethod.PUT, requestEntity1, Object, id)
        then: '状态吗验证成功；参数验证合法'
        response1.statusCode.is2xxSuccessful()
        1 * mockScheduleTaskInstanceService.updateStatus(_)

        when: 'PUT请求【内部接口。更新任务的执行状态】'
        HttpEntity<UpdateTaskInstanceStatusDTO> requestEntity2 = new HttpEntity<>(invalidStatusDTO)
        def response2 = restTemplate.exchange("/v1/schedules/tasks/instances/{id}/status", HttpMethod.PUT, requestEntity2, Object, id)
        then: '状态吗验证成功；参数验证不合法'
        response2.statusCode.is2xxSuccessful()
        0 * mockScheduleTaskInstanceService.updateStatus(_)
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

        and: "构造pageRequest"
        def order = new Sort.Order("id")
        def pageRequest = new PageRequest(0, 20, new Sort(order))

        when: '对接口【分页查询执行方法列表】发送GET请求'
        def entity = restTemplate.getForEntity("/v1/schedules/tasks/instances/{taskId}", Page, taskId, queryParams, pageRequest)
        then: '状态码正确；方法参数调用成功'
        entity.statusCode.is2xxSuccessful()
        1 * mockScheduleTaskInstanceService.pagingQueryByTaskId(_, _, _, _, _, _, _)
    }
}
