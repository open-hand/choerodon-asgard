package io.choerodon.asgard.api.eventhandler

import com.fasterxml.jackson.databind.ObjectMapper
import io.choerodon.asgard.IntegrationTestConfiguration
import io.choerodon.asgard.api.dto.RegisterInstancePayloadDTO
import io.choerodon.asgard.api.service.RegisterInstanceService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class RegisterInstanceListenerSpec extends Specification {

    def "测试 kafka接收的消息的消费"() {
        given: '创建两条ConsumerRecord对象模拟kafka消息'
        def mapper = new ObjectMapper()
        def dto = new RegisterInstancePayloadDTO()
        def skipService = 'api-gateway'
        dto.setStatus(RegisterInstanceListener.STATUS_UP)
        dto.setVersion('v1')
        dto.setInstanceAddress('127.0.0.1:9092')
        dto.setAppName('iam-service')
        dto.setApiData('{}')
        def retryTime = 5
        def upRecord = new ConsumerRecord<byte[], byte[]>('register-server', 1, 999L, ''.getBytes(), mapper.writeValueAsBytes(dto))
        dto.setStatus(RegisterInstanceListener.STATUS_DOWN)
        def downRecord = new ConsumerRecord<byte[], byte[]>('register-server', 1, 999L, ''.getBytes(), mapper.writeValueAsBytes(dto))
        dto.setAppName(skipService)
        def skipRecord = new ConsumerRecord<byte[], byte[]>('register-server', 1, 999L, ''.getBytes(), mapper.writeValueAsBytes(dto))

        and: 'mock registerInstanceService'
        def mockRegisterInstanceService = Mock(RegisterInstanceService)
        def registerInstanceListener = new RegisterInstanceListener(mockRegisterInstanceService)
        registerInstanceListener.setSkipServices([skipService] as String[])
        registerInstanceListener.setSagaFetchTime(retryTime)

        when: '当接收消息为skip的服务消息'
        registerInstanceListener.handle(skipRecord)
        then: 'RegisterInstanceService的方法未执行'
        0 * mockRegisterInstanceService.instanceUpConsumer(_)
        0 * mockRegisterInstanceService.instanceDownConsumer(_)

        when: '当接收消息为UP的服务消息'
        registerInstanceListener.handle(upRecord)
        Thread.sleep(1000)
        then: 'RegisterInstanceService的up方法执行'
        1 * mockRegisterInstanceService.instanceUpConsumer(_)

        when: '当接收消息为DOWN的服务消息'
        registerInstanceListener.handle(downRecord)
        Thread.sleep(1000)
        then: 'RegisterInstanceService的down方法执行'
        1 * mockRegisterInstanceService.instanceDownConsumer(_)


    }
}
