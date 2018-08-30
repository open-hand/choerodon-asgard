package io.choerodon.asgard.api.service

import io.choerodon.asgard.IntegrationTestConfiguration
import io.choerodon.asgard.api.dto.RegisterInstancePayloadDTO
import io.choerodon.asgard.api.service.impl.RegisterInstanceServiceImpl
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
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

        add: 'mock sagaTaskInstanceService'
        def sagaTaskInstanceService = Mock(SagaTaskInstanceService)
        def registerInstanceService = new RegisterInstanceServiceImpl(null, null, sagaTaskInstanceService)

        when: '调用instanceDownConsumer方法'
        registerInstanceService.instanceDownConsumer(dto)

        then: '确认sagaTaskInstanceService的unlockByInstance被执行'
        1 * sagaTaskInstanceService.unlockByInstance(dto.getInstanceAddress())
        0 * sagaTaskInstanceService.unlockByInstance(wrongAddress)
    }

    def '测试 instanceUpConsumer方法'() {

    }

}
