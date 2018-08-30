package io.choerodon.asgard.api.temp

import groovy.json.JsonOutput
import io.choerodon.asgard.IntegrationTestConfiguration
import io.choerodon.asgard.api.dto.PollBatchDTO
import io.choerodon.asgard.api.dto.PollCodeDTO
import io.choerodon.asgard.api.dto.SagaTaskInstanceDTO
import io.choerodon.asgard.api.dto.SagaTaskInstanceStatusDTO
import io.choerodon.asgard.api.service.SagaTaskInstanceService
import io.choerodon.asgard.domain.SagaInstance
import io.choerodon.asgard.domain.SagaTaskInstance
import io.choerodon.asgard.infra.mapper.SagaInstanceMapper
import io.choerodon.asgard.infra.mapper.SagaTaskInstanceMapper
import io.choerodon.asgard.saga.SagaDefinition
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.Transactional
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

/**
 * Created by hailuoliu@choerodon.io on 2018/7/14.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Stepwise
class SagaTaskInstanceServiceSpec extends Specification {

    @Autowired
    SagaTaskInstanceService sagaTaskInstanceService

    @Autowired SagaTaskInstanceMapper sagaTaskInstanceMapper

    @Autowired SagaInstanceMapper sagaInstanceMapper

    @Shared
    def testCode = 'SagaTaskInstanceServiceSpec_task'

    @Shared
    def newPollInstance = 'asgard-test-service-test-instance-new'

    @Shared def sagaTaskInstanceStatusDTO = new SagaTaskInstanceStatusDTO()

    def setup () {
        def sagaInstance = new SagaInstance()
        sagaInstance.setSagaCode('SagaTaskInstanceServiceSpec_saga')
        sagaInstance.setStatus(SagaDefinition.TaskInstanceStatus.RUNNING.name())
        sagaInstanceMapper.insert(sagaInstance)
        def data = sagaInstanceMapper.selectByPrimaryKey(sagaInstance)

        SagaTaskInstance sagaTaskInstance = new SagaTaskInstance()
        sagaTaskInstance.setTaskCode(testCode)
        sagaTaskInstance.setSagaCode('SagaTaskInstanceServiceSpec_saga')
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

    def cleanup() {

    }

    def 'pollBatch'() {
        given: '给定一个批量拉取的DTO'
        def pollInstance = 'asgard-test-service-test-instance'
        PollBatchDTO pollBatchDTO = new PollBatchDTO()
        List<PollCodeDTO> codes = new ArrayList<>()
        pollBatchDTO.setCodes(codes)
        pollBatchDTO.setInstance(pollInstance)

        when: '调用的sagaTaskInstanceService批量拉取方法'
        List<SagaTaskInstanceDTO> pollList = sagaTaskInstanceService.pollBatch(pollBatchDTO)

        then: '验证拉取到的信息；状态为RUNNING'
        pollList.size() == 1
        SagaTaskInstanceDTO sagaTaskInstance = pollList.get(0)
        sagaTaskInstance.getId() != null
        sagaTaskInstance.getStatus() == SagaDefinition.TaskInstanceStatus.RUNNING.name()
        sagaTaskInstanceStatusDTO.setId(sagaTaskInstance.getId())

        then: '换个instance再次拉取，验证拉取不到'
        pollBatchDTO.setInstance(newPollInstance)
        List<SagaTaskInstanceDTO> newPolList = sagaTaskInstanceService.pollBatch(pollBatchDTO)
        newPolList.isEmpty()
    }

    def 'updateStatus'() {
        given: '给定一个更新状态的DTO'
        sagaTaskInstanceStatusDTO.setStatus(SagaDefinition.InstanceStatus.COMPLETED.name())
        sagaTaskInstanceStatusDTO.setOutput(JsonOutput.toJson([name: 'John2', id: 2, pass: 'valJest']))

        when: '调用的sagaTaskInstanceService的更新状态方法'
        sagaTaskInstanceService.updateStatus(sagaTaskInstanceStatusDTO)

        then: '数据库查询验证状态已经更新'
        def data =sagaTaskInstanceMapper.selectByPrimaryKey(sagaTaskInstanceStatusDTO.getId())
        data != null
        data.getStatus() == SagaDefinition.InstanceStatus.COMPLETED.name()
    }

    @Transactional
    def 'unlockByInstance'() {
        given: '数据库插入'
        def sagaTaskInstance = new SagaTaskInstance()
        sagaTaskInstance.setTaskCode('SagaTaskInstanceServiceSpec_lock')
        sagaTaskInstance.setSagaCode('SagaTaskInstanceServiceSpec_saga')
        sagaTaskInstance.setSeq(1)
        sagaTaskInstance.setSagaInstanceId(1L)
        sagaTaskInstance.setMaxRetryCount(1)
        sagaTaskInstance.setTimeoutPolicy(SagaDefinition.TimeoutPolicy.RETRY.name())
        sagaTaskInstance.setTimeoutSeconds(1)
        sagaTaskInstance.setRetriedCount(0)
        sagaTaskInstance.setMaxRetryCount(1)
        sagaTaskInstance.setStatus(SagaDefinition.TaskInstanceStatus.RUNNING.name())
        sagaTaskInstance.setInstanceLock('asgard-test-service-test-instance')
        sagaTaskInstanceMapper.insert(sagaTaskInstance)

        when: '执行unlockByInstance'
        sagaTaskInstanceMapper.unlockByInstance(sagaTaskInstance.getInstanceLock())

        then: '数据库查询有无该lock的数据为空'
        SagaTaskInstance query = new SagaTaskInstance()
        query.setInstanceLock(sagaTaskInstance.getInstanceLock())
        def lockCount = sagaTaskInstanceMapper.selectCount(query)
        lockCount == 0
    }

}
