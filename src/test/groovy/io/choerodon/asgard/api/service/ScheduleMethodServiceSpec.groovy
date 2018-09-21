package io.choerodon.asgard.api.service

import io.choerodon.asgard.IntegrationTestConfiguration
import io.choerodon.asgard.api.dto.ScheduleMethodParamsDTO
import io.choerodon.asgard.api.service.impl.ScheduleMethodServiceImpl
import io.choerodon.asgard.domain.QuartzMethod
import io.choerodon.asgard.infra.mapper.QuartzMethodMapper
import io.choerodon.core.domain.Page
import io.choerodon.core.exception.CommonException
import io.choerodon.mybatis.pagehelper.domain.PageRequest
import io.choerodon.mybatis.pagehelper.domain.Sort
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.context.annotation.Import
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class ScheduleMethodServiceSpec extends Specification {
    @Autowired
    ScheduleMethodServiceImpl scheduleMethodService
    private QuartzMethodMapper mockMethodMapper = Mock(QuartzMethodMapper)

    void setup() {
        scheduleMethodService.setMethodMapper(mockMethodMapper)
    }

    def "PageQuery"() {
        given: "参数准备"
        def code = "code"
        def service = "service"
        def method = "method"
        def description = "description"
        def params = "params"

        and: "构造pageRequest"
        def order = new Sort.Order("id")
        def pageRequest = new PageRequest(1, 2, new Sort(order))

        and: "构造mock返回结果"
        def quartzMethod01 = new QuartzMethod()
        quartzMethod01.setId(1L)
        quartzMethod01.setCode(code)
        quartzMethod01.setService(service)
        quartzMethod01.setMethod(method)
        quartzMethod01.setDescription(description)

        def list = new ArrayList<QuartzMethod>()
        list.add(quartzMethod01)
        and: "mock"
        DiscoveryClient dc = Mock(DiscoveryClient)
        scheduleMethodService.setDiscoveryClient(dc)
        mockMethodMapper.fulltextSearch(_, _, _, _, _) >> { return list }
        dc.getInstances(_) >> { new ArrayList<>() }
        when: "方法调用"
        scheduleMethodService.pageQuery(pageRequest, code, service, method, description, params)
        then: "结果分析"
        thrown(NullPointerException)
    }

    def "pageConvert"() {
        given: "参数准备"
        def code = "code"
        def service = "service"
        def method = "method"
        def description = "description"
        QuartzMethod quartzMethod = new QuartzMethod()
        quartzMethod.setId(1L)
        quartzMethod.setCode(code)
        quartzMethod.setService(service)
        quartzMethod.setMethod(method)
        quartzMethod.setDescription(description)

        List<QuartzMethod> list = new ArrayList<>()
        list.add(quartzMethod)

        def page = new Page<QuartzMethod>()
        page.setNumber(1)
        page.setNumberOfElements(1)
        page.setTotalPages(1)
        page.setTotalElements(1L)

        and: "mock"
        DiscoveryClient dc = Mock(DiscoveryClient)
        scheduleMethodService.setDiscoveryClient(dc)
        dc.getInstances(_) >> { new ArrayList<>() }

        when: "方法调用"
        scheduleMethodService.pageConvert(page)

        then: "结果分析"
        noExceptionThrown()

        and: "content添加"
        page.setContent(list)

        when: "方法调用"
        def convert2 = scheduleMethodService.pageConvert(page)

        then: "结果分析"
        noExceptionThrown()
        convert2.getContent().size() == list.size()
    }

    def "GetMethodByService"() {
        given: "参数准备"
        def serviceName = "serviceName"
        and: "构造mock返回结果"
        def quartzMethod = new QuartzMethod()
        quartzMethod.setId(1L)
        quartzMethod.setMethod("method")
        quartzMethod.setCode("code")
        quartzMethod.setParams("[]")
        def list = new ArrayList<QuartzMethod>()
        list.add(quartzMethod)
        and: "mock"
        mockMethodMapper.selectByService(_) >> { return list }
        when: "方法调用"
        def query = scheduleMethodService.getMethodByService(serviceName)
        then: "结果分析"
        noExceptionThrown()
        query.size() == list.size()
    }

    def "GetParams[MethodNotExist]"() {
        given: "参数准备"
        def id = 1L
        and: "mock"
        mockMethodMapper.selectByPrimaryKey(_) >> { return null }
        when: "方法调用"
        scheduleMethodService.getParams(id)
        then: "结果分析"
        def e = thrown(CommonException)
        e.message == "error.scheduleMethod.notExist"
    }

    def "GetParams"() {
        given: "参数准备"
        def id = 1L
        def quartzMethod = new QuartzMethod()
        quartzMethod.setId(1L)
        def scheduleMethodParamsDTO = new ScheduleMethodParamsDTO()
        scheduleMethodParamsDTO.setId(1L)
        scheduleMethodParamsDTO.setParamsJson("[{\"name\":\"isInstantly\",\"defaultValue\":true,\"type\":\"Boolean\",\"description\":\"测试用布尔类型字段\"},{\"name\":\"name\",\"defaultValue\":\"zh\",\"type\":\"String\",\"description\":\"\"},{\"name\":\"age\",\"defaultValue\":null,\"type\":\"Integer\",\"description\":\"年龄\"}]")

        and: "mock"
        mockMethodMapper.selectByPrimaryKey(_) >> { return quartzMethod }
        mockMethodMapper.selectParamsById(id) >> { return scheduleMethodParamsDTO }

        when: "方法调用"
        scheduleMethodService.getParams(id)

        then: "结果分析"
        noExceptionThrown()
    }

    def "GetParams[jsonIOException]"() {
        given: "参数准备"
        def id = 1L
        def quartzMethod = new QuartzMethod()
        quartzMethod.setId(1L)
        def scheduleMethodParamsDTO = new ScheduleMethodParamsDTO()
        scheduleMethodParamsDTO.setId(1L)
        scheduleMethodParamsDTO.setParamsJson("[invalid]")
        and: "mock"
        mockMethodMapper.selectByPrimaryKey(_) >> { return quartzMethod }
        mockMethodMapper.selectParamsById(id) >> { return scheduleMethodParamsDTO }

        when: "方法调用"
        scheduleMethodService.getParams(id)

        then: "结果分析"
        def e = thrown(CommonException)
        e.message == "error.ScheduleMethodParamsDTO.jsonIOException"
    }
}
