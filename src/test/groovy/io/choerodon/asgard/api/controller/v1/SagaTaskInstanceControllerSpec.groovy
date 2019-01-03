package io.choerodon.asgard.api.controller.v1

import io.choerodon.asgard.IntegrationTestConfiguration
import io.choerodon.asgard.api.dto.SagaTaskInstanceStatusDTO
import io.choerodon.asgard.api.service.SagaTaskInstanceService
import io.choerodon.asgard.saga.dto.PollSagaTaskInstanceDTO
import io.choerodon.core.exception.ExceptionResponse
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
class SagaTaskInstanceControllerSpec extends Specification {

    @Autowired
    SagaTaskInstanceController sagaTaskInstanceController

    @Autowired
    TestRestTemplate testRestTemplate

    def "测试 更新任务的执行状态方法"() {
        given: '创建更新状态的SagaTaskInstanceStatusDTO'
        def statusDTO = new SagaTaskInstanceStatusDTO()
        def id = 10L
        def body = new HttpEntity<SagaTaskInstanceStatusDTO>(statusDTO)

        and: 'mock sagaTaskInstanceService'
        def sagaTaskInstanceService = Mock(SagaTaskInstanceService)
        sagaTaskInstanceController.setSagaTaskInstanceService(sagaTaskInstanceService)

        when: '用status为空的DTO调用接口'

        def invalidEntity = testRestTemplate.exchange("/v1/sagas/tasks/instances/{id}/status",
                HttpMethod.PUT, body, ExceptionResponse, id)
        then: '验证状态码；验证错误状态码正确'
        invalidEntity.statusCode.is2xxSuccessful()
        invalidEntity.body.code == "error.updateStatus.statusEmpty"
        0 * sagaTaskInstanceService.updateStatus(_)

        when: '用合法的DTO调用接口'
        statusDTO.setStatus("status")
        def validEntity = testRestTemplate.exchange("/v1/sagas/tasks/instances/{id}/status",
                HttpMethod.PUT, body, String, id)
        then: '验证状态码；验证错误状态码正确'
        validEntity.statusCode.is2xxSuccessful()
        1 * sagaTaskInstanceService.updateStatus(_)
    }

    def "测试 去除该消息的服务实例锁接口"() {
        given: "设置查询id"
        def id = 1L
        def notId = 10L

        and: 'mock sagaTaskInstanceService'
        def sagaTaskInstanceService = Mock(SagaTaskInstanceService)
        sagaTaskInstanceController.setSagaTaskInstanceService(sagaTaskInstanceService)

        when: '调用接口'
        def entity = testRestTemplate.exchange("/v1/sagas/tasks/instances/{id}/unlock",
                HttpMethod.PUT, new HttpEntity<Object>(), Void, id)

        then: '验证状态码和参数'
        entity.statusCode.is2xxSuccessful()
        0 * sagaTaskInstanceService.unlockById(notId)
        1 * sagaTaskInstanceService.unlockById(id)
    }

    def "测试 根据服务实例批量去除消息的服务实例锁接口"() {
        given: "设置查询id"
        def instance = '127.0.0.1:8080'
        def notInstance = '127.0.0.1:9090'

        and: 'mock sagaTaskInstanceService'
        def sagaTaskInstanceService = Mock(SagaTaskInstanceService)
        sagaTaskInstanceController.setSagaTaskInstanceService(sagaTaskInstanceService)

        when: 'instance参数为空调用接口'
        def invalidEntity = testRestTemplate.exchange("/v1/sagas/tasks/instances/unlock_by_instance?instance={instance}",
                HttpMethod.PUT, new HttpEntity<Object>(), ExceptionResponse, "")
        then: '验证状态码和参数'
        invalidEntity.statusCode.is2xxSuccessful()
        invalidEntity.body.code == "error.unlockByInstance.instanceEmpty"
        0 * sagaTaskInstanceService.unlockByInstance(_)

        when: '调用接口'
        def entity = testRestTemplate.exchange("/v1/sagas/tasks/instances/unlock_by_instance?instance={instance}",
                HttpMethod.PUT, new HttpEntity<Object>(), Void, instance)
        then: '验证状态码和参数'
        entity.statusCode.is2xxSuccessful()
        0 * sagaTaskInstanceService.unlockByInstance(notInstance)
        1 * sagaTaskInstanceService.unlockByInstance(instance)
    }

    def "测试 手动重试消息接口"() {
        given: "设置查询id"
        def id = 1L
        def notId = 10L

        and: 'mock sagaTaskInstanceService'
        def sagaTaskInstanceService = Mock(SagaTaskInstanceService)
        sagaTaskInstanceController.setSagaTaskInstanceService(sagaTaskInstanceService)

        when: '调用接口'
        def entity = testRestTemplate.exchange("/v1/sagas/tasks/instances/{id}/retry",
                HttpMethod.PUT, new HttpEntity<Object>(), Void, id)

        then: '验证状态码和参数'
        entity.statusCode.is2xxSuccessful()
        0 * sagaTaskInstanceService.retry(notId)
        1 * sagaTaskInstanceService.retry(id)
    }


    def "测试 平台层分页查询SagaTask实例列表"() {
        given: '设置查询参数'
        def sagaInstanceCode = 'sagaInstanceCode'
        def status = 'status'
        def taskInstanceCode = 'taskInstanceCode'
        def params = 'params'
        def wrongParam = 'param'

        and: 'mock sagaTaskInstanceService'
        def sagaTaskInstanceService = Mock(SagaTaskInstanceService)
        sagaTaskInstanceController.setSagaTaskInstanceService(sagaTaskInstanceService)

        when: "调用查询事务列表接口"
        def entity = testRestTemplate.getForEntity("/v1/sagas/tasks/instances?sagaInstanceCode={sagaInstanceCode}" +
                "&status={status}&taskInstanceCode={taskInstanceCode}&params={params}",
                String, sagaInstanceCode, status, taskInstanceCode, params)

        then: "验证状态码成功；验证查询参数生效"
        entity.statusCode.is2xxSuccessful()
        1 * sagaTaskInstanceService.pageQuery(_, sagaInstanceCode, status, taskInstanceCode, params, null, null)
        0 * sagaTaskInstanceService.pageQuery(_, sagaInstanceCode, status, taskInstanceCode, wrongParam, null, null)
    }
}
