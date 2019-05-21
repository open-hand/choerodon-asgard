package io.choerodon.asgard.api.controller.v1

import io.choerodon.asgard.IntegrationTestConfiguration
import io.choerodon.asgard.api.dto.SagaWithTaskDTO
import io.choerodon.asgard.api.service.SagaService
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
class SagaControllerSpec extends Specification {

    @Autowired
    TestRestTemplate testRestTemplate

    @Autowired
    SagaController sagaController

    def "测试 查询事务列表接口"() {
        given: "设置查询参数"
        def code = "code"
        def description = "desc"
        def service = "sv"
        def params = "params"
        def notParams = "param"

        and: "mock sagaService"
        def sagaService = Mock(SagaService)
        sagaController.setSagaService(sagaService)

        when: "调用查询事务列表接口"
        def entity = testRestTemplate.getForEntity(
                "/v1/sagas?code={code}&description={description}&service={service}&params={params}",
                String, code, description, service, params)

        then: "验证状态码成功；验证查询参数生效"
        entity.statusCode.is2xxSuccessful()
        1 * sagaService.pagingQuery(_,_, code, description, service, params)
        0 * sagaService.pagingQuery(_,_, code, description, service, notParams)
    }

    def "测试 查询某个事务的定义详情"() {
        given: "设置查询id"
        def id = 1L
        def notId = 10L

        and: "mock sagaService"
        def sagaService = Mock(SagaService)
        sagaController.setSagaService(sagaService)

        when: "调用查询事务列表接口"
        def entity = testRestTemplate.getForEntity("/v1/sagas/{id}", SagaWithTaskDTO, id)

        then: "验证状态码成功；验证查询参数生效"
        entity.statusCode.is2xxSuccessful()
        1 * sagaService.query(id)
        0 * sagaService.query(notId)
    }

    def "测试 删除事务"() {
        given: "设置查询id"
        def id = 1L
        def notId = 10L

        and: "mock sagaService"
        def sagaService = Mock(SagaService)
        sagaController.setSagaService(sagaService)

        when: "调用删除事务接口"
        def entity = testRestTemplate.exchange("/v1/sagas/{id}", HttpMethod.DELETE, new HttpEntity<Object>(), Void, id)

        then: "验证状态码成功；验证查询参数生效"
        entity.statusCode.is2xxSuccessful()
        1 * sagaService.delete(id)
        0 * sagaService.delete(notId)
    }

}
