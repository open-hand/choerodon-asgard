package io.choerodon.asgard.api.controller

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.json.JsonOutput
import io.choerodon.asgard.IntegrationTestConfiguration
import io.choerodon.asgard.api.dto.PollBatchDTO
import io.choerodon.asgard.api.dto.SagaTaskInstanceDTO
import io.choerodon.asgard.api.dto.SagaTaskInstanceStatusDTO
import io.choerodon.asgard.domain.SagaInstance
import io.choerodon.asgard.domain.SagaTaskInstance
import io.choerodon.asgard.infra.mapper.SagaInstanceMapper
import io.choerodon.asgard.infra.mapper.SagaTaskInstanceMapper
import io.choerodon.asgard.saga.SagaDefinition
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

/**
 * Created by hailuoliu@choerodon.io on 2018/7/14.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Stepwise
class SagaTaskInstanceControllerSpec extends Specification {

    @Autowired
    TestRestTemplate restTemplate

    private final ObjectMapper objectMapper = new ObjectMapper()

    @Shared
    SagaTaskInstanceDTO taskInstanceDTO = new SagaTaskInstanceDTO()

    @Autowired
    SagaInstanceMapper instanceMapper

    @Autowired SagaTaskInstanceMapper sagaTaskInstanceMapper

    @Autowired SagaInstanceMapper sagaInstanceMapper

    @Shared
    def testCode = 'SagaTaskInstanceControllerSpec'

    def setup () {
        SagaInstance sagaInstance = new SagaInstance()
        sagaInstance.setSagaCode(testCode)
        sagaInstance.setStatus(SagaDefinition.TaskInstanceStatus.RUNNING.name())
        sagaInstanceMapper.insert(sagaInstance)
        def data = sagaInstanceMapper.selectByPrimaryKey(sagaInstance)

        SagaTaskInstance sagaTaskInstance = new SagaTaskInstance()
        sagaTaskInstance.setTaskCode(testCode)
        sagaTaskInstance.setSagaCode('SagaTaskInstanceControllerSpec_saga')
        sagaTaskInstance.setSeq(1)
        sagaTaskInstance.setSagaInstanceId(data.getId())
        sagaTaskInstance.setMaxRetryCount(1)
        sagaTaskInstance.setTimeoutPolicy(SagaDefinition.TimeoutPolicy.RETRY.name())
        sagaTaskInstance.setTimeoutSeconds(1)
        sagaTaskInstance.setRetriedCount(0)
        sagaTaskInstance.setMaxRetryCount(1)
        sagaTaskInstance.setStatus(SagaDefinition.TaskInstanceStatus.RUNNING.name())
        sagaTaskInstanceMapper.insert(sagaTaskInstance)

    }

    def 'PollBatch'() {
        given: '给定一个批量拉取的DTO'
        PollBatchDTO pollBatchDTO = new PollBatchDTO()
        pollBatchDTO.setInstance('asgard-test-service-test-instance')

        when: '向批量拉取task消息的接口发送请求'
        def entity = restTemplate.postForEntity('/v1/saga/tasks/instances/poll/batch', pollBatchDTO, String)

        then: '状态码为200;拉取消息数目大于0;消息中的状态为RUNNING'
        entity.statusCode.is2xxSuccessful()
        JavaType javaType = objectMapper.getTypeFactory().constructCollectionType(ArrayList, SagaTaskInstanceDTO)
        List<SagaTaskInstanceDTO> list = (List<SagaTaskInstanceDTO>) objectMapper.readValue(entity.body, javaType)
        list.size() == 1
        SagaTaskInstanceDTO temp = list.get(0)
        taskInstanceDTO.setStatus(temp.getStatus())
        taskInstanceDTO.setId(temp.getId())
        taskInstanceDTO.getStatus() == SagaDefinition.InstanceStatus.RUNNING.name()
    }

    def 'UpdateStatus'() {
        given: '给定一个更新状态的DTO'
        SagaTaskInstanceStatusDTO sagaTaskInstanceStatusDTO = new SagaTaskInstanceStatusDTO()
        sagaTaskInstanceStatusDTO.setId(taskInstanceDTO.getId())
        sagaTaskInstanceStatusDTO.setStatus(SagaDefinition.InstanceStatus.COMPLETED.name())
        sagaTaskInstanceStatusDTO.setOutput(JsonOutput.toJson([name: 'John1', id: 2, pass: 'valJest']))

        when: '向更新状态接口发送PUT请求'
        HttpEntity<SagaTaskInstanceStatusDTO> httpEntity = new HttpEntity<>(sagaTaskInstanceStatusDTO)
        def entity = restTemplate.exchange('/v1/saga/tasks/instances/{id}/status', HttpMethod.PUT, httpEntity, Void, sagaTaskInstanceStatusDTO.getId())

        then: '状态码为200'
        entity.statusCode.is2xxSuccessful()

    }
}
