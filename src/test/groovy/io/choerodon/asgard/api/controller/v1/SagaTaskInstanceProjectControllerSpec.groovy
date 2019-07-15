package io.choerodon.asgard.api.controller.v1

import io.choerodon.asgard.IntegrationTestConfiguration
import io.choerodon.asgard.app.service.SagaTaskInstanceService
import io.choerodon.core.iam.ResourceLevel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class SagaTaskInstanceProjectControllerSpec extends Specification {

    @Autowired
    SagaTaskInstanceProjController sagaTaskInstanceController

    @Autowired
    TestRestTemplate testRestTemplate


    def "测试 组织层分页查询SagaTask实例列表"() {
        given: '设置查询参数'
        def sagaInstanceCode = 'sagaInstanceCode'
        def status = 'status'
        def taskInstanceCode = 'taskInstanceCode'
        def params = 'params'
        def wrongParam = 'param'
        def projectId = 1

        and: 'mock sagaTaskInstanceService'
        def sagaTaskInstanceService = Mock(SagaTaskInstanceService)
        sagaTaskInstanceController.setSagaTaskInstanceService(sagaTaskInstanceService)

        when: "调用查询事务列表接口"
        def entity = testRestTemplate.getForEntity("/v1/sagas/projects/{project_id}/tasks/instances?sagaInstanceCode={sagaInstanceCode}" +
                "&status={status}&taskInstanceCode={taskInstanceCode}&params={params}",
                String, projectId, sagaInstanceCode, status, taskInstanceCode, params)

        then: "验证状态码成功；验证查询参数生效"
        entity.statusCode.is2xxSuccessful()
        1 * sagaTaskInstanceService.pageQuery(_, _, sagaInstanceCode, status, taskInstanceCode, params, ResourceLevel.PROJECT.value(), projectId)
        0 * sagaTaskInstanceService.pageQuery(_, _, sagaInstanceCode, status, taskInstanceCode, wrongParam, ResourceLevel.PROJECT.value(), projectId)
    }
}
