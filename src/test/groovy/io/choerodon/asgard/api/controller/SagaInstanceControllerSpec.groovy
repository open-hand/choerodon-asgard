package io.choerodon.asgard.api.controller

import groovy.json.JsonOutput
import io.choerodon.asgard.IntegrationTestConfiguration
import io.choerodon.asgard.api.dto.StartInstanceDTO
import io.choerodon.asgard.api.service.SagaService
import io.choerodon.asgard.api.service.SagaTaskService
import io.choerodon.asgard.domain.Saga
import io.choerodon.asgard.domain.SagaInstance
import io.choerodon.asgard.domain.SagaTask
import io.choerodon.core.saga.SagaDefinition
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import spock.lang.Shared
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * Created by hailuoliu@choerodon.io on 2018/7/12.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class SagaInstanceControllerSpec extends Specification {

    @Autowired
    TestRestTemplate restTemplate

    @Autowired SagaService sagaService

    @Autowired SagaTaskService taskService


    @Shared def createUserSagaCode = 'create-user'

    def setup() {
        Saga saga = new Saga()
        saga.setService('asgard-test-service')
        saga.setCode(createUserSagaCode)
        saga.setDescription('测试创建用户')
        sagaService.createSaga(saga)

        SagaTask step1 = new SagaTask('devops-user', createUserSagaCode, 2, true, saga.getService())
        SagaTask step2 = new SagaTask('agile-user', createUserSagaCode, 5, true, saga.getService())
        taskService.createSagaTaskList(Arrays.asList(step1, step2), saga.getService())
    }

    def 'StartSaga'() {
        given: '给定一个开始saga的DTO'
        StartInstanceDTO startInstanceDTO = new StartInstanceDTO()
        startInstanceDTO.setSagaCode(createUserSagaCode)
        def inputJson = JsonOutput.toJson([name: 'John', id: 1, pass: 'valJest'])
        startInstanceDTO.setInput(inputJson)

        when: '向开始saga的接口发请求'
        def entity = restTemplate.postForEntity('/v1/saga/{code}/instances', startInstanceDTO, SagaInstance, createUserSagaCode)

        then: '状态码为200;saga的状态为RUNNING'
        entity.statusCode.is2xxSuccessful()
        entity.body.getSagaCode() == createUserSagaCode
        entity.body.getStatus() == SagaDefinition.InstanceStatus.STATUS_RUNNING.name()
    }


}
