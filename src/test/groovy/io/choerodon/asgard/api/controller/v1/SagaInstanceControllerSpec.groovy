package io.choerodon.asgard.api.controller.v1

import io.choerodon.asgard.IntegrationTestConfiguration
import io.choerodon.asgard.api.vo.SagaInstanceDetails
import io.choerodon.asgard.api.vo.SagaWithTaskInstance
import io.choerodon.asgard.api.vo.StartInstance
import io.choerodon.asgard.app.service.SagaInstanceService
import io.choerodon.asgard.app.service.impl.SagaInstanceServiceImpl
import io.choerodon.asgard.infra.dto.SagaInstanceDTO
import io.choerodon.asgard.infra.dto.SagaTaskDTO
import io.choerodon.asgard.infra.mapper.SagaInstanceMapper
import io.choerodon.asgard.infra.mapper.SagaTaskMapper
import io.choerodon.core.iam.ResourceLevel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.Transactional
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class SagaInstanceControllerSpec extends Specification {

    @Autowired
    TestRestTemplate testRestTemplate
    @Autowired
    private SagaInstanceMapper sagaInstanceMapper
    @Autowired
    private SagaInstanceService sagaInstanceService

    @Autowired
    SagaInstanceController sagaInstanceController

    def "测试 开始saga接口"() {
        given: "创建请求DTO"
        def code = "code"
        def dto = new StartInstance()
        dto.setInput("in")
        dto.setRefId("id")
        dto.setRefType("type")

        and: "mock sagaInstanceService"
        def sagaInstanceService = Mock(SagaInstanceService)
        sagaInstanceController.setSagaInstanceService(sagaInstanceService)

        when: "用合法的DTO调用开始saga接口"
        def entity = testRestTemplate.postForEntity(
                "/v1/sagas/instances/{code}",
                dto, io.choerodon.asgard.api.vo.SagaInstance, code)

        then: "验证状态码成功；验证查询参数生效"
        entity.statusCode.is2xxSuccessful()
        1 * sagaInstanceService.start(_)
    }

    def "测试 查询事务实例列表接口"() {
        given: '设置查询参数'
        def sagaCode = 'sagaCode'
        def status = 'status'
        def refType = 'refType'
        def refId = 'refId'
        def params = 'params'
        def wrongParam = 'param'

        and: "mock sagaInstanceService"
        def sagaInstanceService = Mock(SagaInstanceService)
        sagaInstanceController.setSagaInstanceService(sagaInstanceService)

        when: "调用查询事务列表接口"
        def entity = testRestTemplate.getForEntity("/v1/sagas/instances?sagaCode={sagaCode}" +
                "&status={status}&refType={refType}&refId={refId}&params={params}",
                String, sagaCode, status, refType, refId, params)

        then: "验证状态码成功；验证查询参数生效"
        entity.statusCode.is2xxSuccessful()
        1 * sagaInstanceService.pageQuery(_, _, sagaCode, status, refType, refId, params, _, _)
        0 * sagaInstanceService.pageQuery(_, _, sagaCode, status, refType, refId, wrongParam, _, _)
    }

    def "测试 查询某个事务实例运行详情接口"() {
        given: "设置查询id"
        def id = 1L
        def notId = 10L

        and: "mock sagaInstanceService"
        def sagaInstanceService = Mock(SagaInstanceService)
        sagaInstanceController.setSagaInstanceService(sagaInstanceService)

        when: "调用查询事务列表接口"
        def entity = testRestTemplate.getForEntity("/v1/sagas/instances/{id}", SagaWithTaskInstance, id)

        then: "验证状态码成功；验证查询参数生效"
        entity.statusCode.is2xxSuccessful()
        1 * sagaInstanceService.query(id)
        0 * sagaInstanceService.query(notId)
    }

    def "测试 查询事务实例的具体信息"() {
        given: "设置查询id"
        def id = 1L
        def notId = 10L

        and: "mock sagaInstanceService"
        def sagaInstanceService = Mock(SagaInstanceService)
        sagaInstanceController.setSagaInstanceService(sagaInstanceService)

        when: "调用查询事务列表接口"
        def entity = testRestTemplate.getForEntity("/v1/sagas/instances/{id}/details", SagaInstanceDetails, id)

        then: "验证状态码成功；验证查询参数生效"
        entity.statusCode.is2xxSuccessful()
        1 * sagaInstanceService.queryDetails(id)
        0 * sagaInstanceService.queryDetails(notId)
    }

    def "测试 统计全平台各个事务实例状态下的实例个数"() {

        given: "mock sagaInstanceService"
        def sagaInstanceService = Mock(SagaInstanceService)
        sagaInstanceController.setSagaInstanceService(sagaInstanceService)

        when: "调用查询事务列表接口"
        def entity = testRestTemplate.getForEntity("/v1/sagas/instances/statistics", Map)

        then: "验证状态码成功；验证查询参数生效"
        entity.statusCode.is2xxSuccessful()
        1 * sagaInstanceService.statistics(null, null)
    }

    @Transactional
    def "preCreate"() {
        given:
        StartInstance dto = new StartInstance()
        dto.setUuid("uuid")
        dto.setSagaCode("code")
        dto.setService("service")
        dto.setLevel(ResourceLevel.PROJECT.value())
        dto.setSourceId(1L)

        and:
        SagaInstanceController controller = new SagaInstanceController(sagaInstanceService)

        when:
        def result = controller.preCreate(dto)

        then:
        result.statusCode.is2xxSuccessful()
    }

    def "confirm"() {
        given:
        StartInstance dto = new StartInstance()
        dto.setRefId("ref")
        dto.setInput("input")
        dto.setRefType("type")

        and:
        SagaInstanceMapper mapper = Mock(SagaInstanceMapper)
        SagaTaskMapper sagaTaskMapper = Mock(SagaTaskMapper)
        SagaInstanceService service = new SagaInstanceServiceImpl(sagaTaskMapper, mapper, null, null, null, null)
        SagaInstanceController controller = new SagaInstanceController(service)

        when:
        controller.confirm("uuid", dto)

        then:
        1 * mapper.selectOne(_) >> Mock(SagaInstanceDTO)
        1 * mapper.updateByPrimaryKey(_) >> 1
        1 * sagaTaskMapper.selectFirstSeqSagaTasks(_) >> new ArrayList<SagaTaskDTO>()
    }

    def "queryFailedByDate"() {
        given:
        SagaInstanceMapper mapper = Mock(SagaInstanceMapper)
        List<Map<String, Object>> list = new ArrayList<>()
        Map<String, Object> map = new HashMap<>()
        map.put("days", new Object())
        list << map
        mapper.selectFailedTimes(_, _) >> list
        and:
        SagaInstanceService service = new SagaInstanceServiceImpl(null, mapper, null, null, null, null)
        SagaInstanceController controller = new SagaInstanceController(service)

        when:
        def result = controller.queryFailedByDate("2019-03-15", "2019-03-20")

        then:
        result.statusCode.is2xxSuccessful()

    }
}
