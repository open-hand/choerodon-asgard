package io.choerodon.asgard.api.service

import io.choerodon.asgard.IntegrationTestConfiguration
import io.choerodon.asgard.api.dto.RegisterInstancePayloadDTO
import io.choerodon.asgard.api.service.impl.RegisterInstanceServiceImpl
import io.choerodon.asgard.property.PropertyData
import io.choerodon.asgard.property.PropertySaga
import io.choerodon.asgard.property.PropertySagaTask
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.remoting.RemoteAccessException
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class RegisterInstanceServiceSpec extends Specification {

    def '测试 instanceDownConsumer方法'() {
        given: '创建一个RegisterInstancePayloadDTO对象'
        def dto = new RegisterInstancePayloadDTO()
        def wrongAddress = '127.0.0.1:8080'
        dto.setInstanceAddress('127.0.0.1:9092')

        and: "mock sagaTaskInstanceService"
        def sagaTaskInstanceService = Mock(SagaTaskInstanceService)
        def scheduleTaskInstanceService = Mock(ScheduleTaskInstanceService)
        def registerInstanceService = new RegisterInstanceServiceImpl(null, null, sagaTaskInstanceService, null, scheduleTaskInstanceService)

        when: '调用instanceDownConsumer方法'
        registerInstanceService.instanceDownConsumer(dto)

        then: '确认sagaTaskInstanceService的unlockByInstance被执行'
        1 * sagaTaskInstanceService.unlockByInstance(dto.getInstanceAddress())
        0 * sagaTaskInstanceService.unlockByInstance(wrongAddress)
        1 * scheduleTaskInstanceService.unlockByInstance(dto.getInstanceAddress())
        0 * scheduleTaskInstanceService.unlockByInstance(wrongAddress)
    }

    def '测试 instanceUpConsumer方法'() {
        given: '创建RegisterInstancePayloadDTO和PropertyData对象'
        def dto = new RegisterInstancePayloadDTO()
        dto.setInstanceAddress('127.0.0.1:9092')
        def propertyData = new PropertyData()
        propertyData.setService('test-asgard-service')
        def saga = new PropertySaga()
        saga.setCode('instanceUpConsumer')
        propertyData.addSaga(saga)
        propertyData.addSagaTask(new PropertySagaTask("instanceUpConsumerTask", "test", "instanceUpConsumer", 2, 4))

        and: "mock service和restTemplate"
        def sagaService = Mock(SagaService)
        def sagaTaskService = Mock(SagaTaskService)
        def quartzMethodService = Mock(QuartzMethodService)
        def correctRestTemplate = Stub(RestTemplate) {
            getForEntity(_, _) >>> new ResponseEntity<PropertyData>(propertyData, HttpStatus.OK)
        }
        def errorRestTemplate = Stub(RestTemplate) {
            getForEntity(_, _) >>> new ResponseEntity<PropertyData>(HttpStatus.INTERNAL_SERVER_ERROR)
        }
        def registerInstanceService = new RegisterInstanceServiceImpl(sagaService, sagaTaskService, null, quartzMethodService, null)

        when: '使用correctRestTemplate调用instanceUpConsumer方法'
        registerInstanceService.setRestTemplate(correctRestTemplate)
        registerInstanceService.setLocal(false)
        registerInstanceService.instanceUpConsumer(dto)

        then: '验证service调用'
        1 * sagaService.create(_)
        1 * sagaTaskService.createSagaTaskList(_, _)

        when: '使用errorRestTemplate调用instanceUpConsumer方法'
        registerInstanceService.setRestTemplate(errorRestTemplate)
        registerInstanceService.setLocal(true)
        registerInstanceService.instanceUpConsumer(dto)

        then: '验证service调用'
        thrown RemoteAccessException

    }

}
