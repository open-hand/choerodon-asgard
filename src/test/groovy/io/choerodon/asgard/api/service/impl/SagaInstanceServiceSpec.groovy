package io.choerodon.asgard.api.service.impl

import groovy.json.JsonOutput
import io.choerodon.asgard.IntegrationTestConfiguration
import io.choerodon.asgard.api.dto.StartInstanceDTO
import io.choerodon.asgard.api.service.SagaInstanceService
import io.choerodon.asgard.api.service.SagaService
import io.choerodon.asgard.api.service.SagaTaskService
import io.choerodon.asgard.domain.Saga
import io.choerodon.asgard.domain.SagaInstance
import io.choerodon.asgard.domain.SagaTask
import io.choerodon.asgard.domain.SagaTaskInstance
import io.choerodon.asgard.infra.mapper.SagaInstanceMapper
import io.choerodon.asgard.infra.mapper.SagaTaskInstanceMapper
import io.choerodon.asgard.saga.SagaDefinition
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import spock.lang.Shared
import spock.lang.Specification

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class SagaInstanceServiceSpec extends Specification {

    @Autowired
    SagaInstanceService sagaInstanceService

    @Autowired SagaService sagaService

    @Autowired SagaTaskService taskService

    @Autowired
    SagaTaskInstanceMapper sagaTaskInstanceMapper

    @Autowired
    SagaInstanceMapper sagaInstanceMapper

    @Shared
    def createUserSagaCode = 'SagaInstance-create-user'

    @Shared
    def devopsTaskCode = 'SagaInstance-devops-user'

    @Shared
    def agileTaskCode = 'SagaInstance-agile-user'

    def setup () {
        Saga saga = new Saga()
        saga.setService('SagaInstance-Service')
        saga.setCode(createUserSagaCode)
        saga.setDescription('测试创建用户')
        sagaService.createSaga(saga)

        SagaTask step1 = new SagaTask(devopsTaskCode, createUserSagaCode, 2, true, saga.getService())
        SagaTask step2 = new SagaTask(agileTaskCode, createUserSagaCode, 5, true, saga.getService())
        taskService.createSagaTaskList(Arrays.asList(step1, step2), saga.getService())
    }


    def 'start'() {
        given: '给定一个开始saga的DTO'
        StartInstanceDTO startInstanceDTO = new StartInstanceDTO()
        startInstanceDTO.setSagaCode(createUserSagaCode)
        def inputJson = JsonOutput.toJson([name: 'Li', id: 1, pass: 'valJest'])
        startInstanceDTO.setInput(inputJson)

        when: '调用SagaInstanceService的start方法'
        sagaInstanceService.start(startInstanceDTO)

        then: '数据库查看是否已有instance和taskInstance数据,并验证'
        SagaInstance instance = sagaInstanceMapper.selectOne(new SagaInstance(createUserSagaCode))
        instance != null
        instance.getStatus() == SagaDefinition.InstanceStatus.RUNNING.name()
        instance.getSagaCode() == createUserSagaCode

        SagaTaskInstance devopsTaskInstance = sagaTaskInstanceMapper.selectOne(new SagaTaskInstance(devopsTaskCode))
        devopsTaskInstance != null
        devopsTaskInstance.getStatus() == SagaDefinition.TaskInstanceStatus.RUNNING.name()
        devopsTaskInstance.getSagaCode() == createUserSagaCode
        devopsTaskInstance.getTaskCode() == devopsTaskCode
        devopsTaskInstance.getSagaInstanceId() == instance.getId()

        SagaTaskInstance agileTaskInstance = sagaTaskInstanceMapper.selectOne(new SagaTaskInstance(agileTaskCode))
        agileTaskInstance != null
        agileTaskInstance.getStatus() == SagaDefinition.TaskInstanceStatus.QUEUE.name()
        agileTaskInstance.getSagaCode() == createUserSagaCode
        agileTaskInstance.getTaskCode() == agileTaskCode
        agileTaskInstance.getSagaInstanceId() == instance.getId()
    }

}
