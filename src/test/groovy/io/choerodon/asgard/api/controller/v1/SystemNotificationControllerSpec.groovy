package io.choerodon.asgard.api.controller.v1

import io.choerodon.asgard.IntegrationTestConfiguration
import io.choerodon.asgard.api.dto.SystemNotificationCreateDTO
import io.choerodon.asgard.api.dto.SystemNotificationDTO
import io.choerodon.asgard.api.service.ScheduleTaskService
import io.choerodon.asgard.api.service.SystemNocificationService
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
class SystemNotificationControllerSpec extends Specification {
    public static final String BASE_PATH = "/v1/system_notice"
    @Autowired
    TestRestTemplate restTemplate

    @Autowired
    private SystemNotificationController systemNotificationController

    private SystemNocificationService mockSystemNocificationService = Mock(SystemNocificationService)
    private ScheduleTaskService mockScheduleTaskService = Mock(ScheduleTaskService)


    void setup() {
        systemNotificationController.setScheduleTaskService(mockScheduleTaskService)
        systemNotificationController.setSystemNocificationService(mockSystemNocificationService)
    }

    def "CreateNotificationOnSite"() {
        given: "参数准备"
        def snDTO = new SystemNotificationCreateDTO()
        snDTO.setContent("content")
        snDTO.setStartTime(new Date())

        when: "POST请求【创建系统公告】"
        def entity = restTemplate.postForEntity(BASE_PATH + "/create", snDTO, SystemNotificationDTO)

        then: "无异常抛出"
        noExceptionThrown()
        entity.statusCode.is2xxSuccessful()
    }

    def "DeleteSiteNotification"() {
        given: "准备参数"
        def taskId = 1L
        when: 'DELETE请求【删除系统公告】'
        HttpEntity<Object> httpEntity = new HttpEntity<>()
        def response = restTemplate.exchange(BASE_PATH + '/delete?taskId={taskId}', HttpMethod.DELETE, httpEntity, Void, taskId)
        then: '状态码验证通过；验证方法参数生效'
        noExceptionThrown()
        response.statusCode.is2xxSuccessful()
    }

    def "GetSiteNotificationDetails"() {
        given: "准备参数"
        def id = 1
        when: "GET请求【全局层查看公告详情】"
        def entity = restTemplate.getForEntity(BASE_PATH + "/detail/{id}", SystemNotificationDTO, id)
        then: "结果比对"
        noExceptionThrown()
        entity.statusCode.is2xxSuccessful()
    }

    def "PagingQuerySiteNotification"() {
        given: "参数准备"
        def status = null
        def content = "content"
        def params = "params"
        def query = new HashMap<String, String>()
        query.put("status", status)
        query.put("content", content)
        query.put("params", params)

        and: "构造pageRequest"
        def order = new Sort.Order("start_time")
        def pageRequest = new PageRequest(0, 20, new Sort(order))
        query.put("pageRequest", pageRequest)

        when: "GET请求【全局层分页查询系统公告】"
        def entity = restTemplate.getForEntity(BASE_PATH + "/all", Page, query)
        then: "结果比对"
        noExceptionThrown()
        entity.statusCode.is2xxSuccessful()
    }
}
