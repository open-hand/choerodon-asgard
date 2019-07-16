package io.choerodon.asgard.api.controller.v1

import io.choerodon.asgard.IntegrationTestConfiguration
import io.choerodon.asgard.api.vo.SagaInstance
import io.choerodon.asgard.api.vo.SagaTaskInstance
import io.choerodon.asgard.app.service.NoticeService
import io.choerodon.asgard.app.service.SagaTaskInstanceService
import io.choerodon.asgard.app.service.impl.SagaTaskInstanceServiceImpl
import io.choerodon.asgard.infra.dto.SagaInstanceDTO
import io.choerodon.asgard.infra.dto.SagaTaskInstanceDTO
import io.choerodon.asgard.infra.mapper.SagaInstanceMapper
import io.choerodon.asgard.infra.mapper.SagaTaskInstanceMapper
import io.choerodon.core.iam.ResourceLevel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class SagaTaskInstanceOrgControllerSpec extends Specification {

    @Autowired
    SagaTaskInstanceOrgController sagaTaskInstanceController

    @Autowired
    TestRestTemplate testRestTemplate
    @Autowired
    DataSourceTransactionManager transactionManager


    def "测试 组织层分页查询SagaTask实例列表"() {
        given: '设置查询参数'
        def sagaInstanceCode = 'sagaInstanceCode'
        def status = 'status'
        def taskInstanceCode = 'taskInstanceCode'
        def params = 'params'
        def wrongParam = 'param'
        def orgId = 1

        and: 'mock sagaTaskInstanceService'
        def sagaTaskInstanceService = Mock(SagaTaskInstanceService)
        sagaTaskInstanceController.setSagaTaskInstanceService(sagaTaskInstanceService)

        when: "调用查询事务列表接口"
        def entity = testRestTemplate.getForEntity("/v1/sagas/organizations/{organization_id}/tasks/instances?sagaInstanceCode={sagaInstanceCode}" +
                "&status={status}&taskInstanceCode={taskInstanceCode}&params={params}",
                String, orgId, sagaInstanceCode, status, taskInstanceCode, params)

        then: "验证状态码成功；验证查询参数生效"
        entity.statusCode.is2xxSuccessful()
        1 * sagaTaskInstanceService.pageQuery(_,_, sagaInstanceCode, status, taskInstanceCode, params, ResourceLevel.ORGANIZATION.value(), orgId)
        0 * sagaTaskInstanceService.pageQuery(_,_, sagaInstanceCode, status, taskInstanceCode, wrongParam, ResourceLevel.ORGANIZATION.value(), orgId)
    }

    def "unlockById"() {
        given:
        SagaTaskInstanceMapper sagaTaskInstanceMapper = Mock(SagaTaskInstanceMapper)
        SagaTaskInstanceService sagaTaskInstanceService = new SagaTaskInstanceServiceImpl(sagaTaskInstanceMapper, null, null, null, null, null, null, null)
        SagaTaskInstanceOrgController controller = new SagaTaskInstanceOrgController(sagaTaskInstanceService)

        when:
        controller.unlockById(1L, 1L)

        then:
        1 * sagaTaskInstanceMapper.selectByPrimaryKey(_) >> Mock(SagaTaskInstanceDTO)
        1 * sagaTaskInstanceMapper.updateByPrimaryKey(_)
    }

    def "forceFailed"() {
        given:
        SagaTaskInstanceMapper sagaTaskInstanceMapper = Mock(SagaTaskInstanceMapper)
        SagaInstanceMapper sagaInstanceMapper = Mock(SagaInstanceMapper)
        NoticeService noticeService = Mock(NoticeService)
        SagaTaskInstanceService sagaTaskInstanceService = new SagaTaskInstanceServiceImpl(sagaTaskInstanceMapper, sagaInstanceMapper, null, transactionManager, noticeService, null, null, null)
        SagaTaskInstanceOrgController controller = new SagaTaskInstanceOrgController(sagaTaskInstanceService)

        and:
        SagaTaskInstanceDTO sagaTaskInstance = Mock(SagaTaskInstanceDTO)
        SagaInstanceDTO sagaInstance = Mock(SagaInstanceDTO)

        when:
        controller.forceFailed(1L, 1L)

        then:
        1 * sagaTaskInstanceMapper.selectByPrimaryKey(_) >> sagaTaskInstance
        1 * sagaInstanceMapper.selectByPrimaryKey(_) >> sagaInstance
        1 * sagaTaskInstanceMapper.updateByPrimaryKey(_) >> 1
        1 * sagaInstanceMapper.updateByPrimaryKeySelective(_) >> 1
        1 * sagaInstance.getSagaCode() >> "register-org"
        1 * noticeService.registerOrgFailNotice(_, _)
        1 * sagaInstance.getCreatedBy() >> 1
        1 * noticeService.sendSagaFailNotice(_)
    }
}
