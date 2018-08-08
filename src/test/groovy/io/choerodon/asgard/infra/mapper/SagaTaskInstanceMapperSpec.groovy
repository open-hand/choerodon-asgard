package io.choerodon.asgard.infra.mapper

import io.choerodon.asgard.IntegrationTestConfiguration
import io.choerodon.asgard.domain.SagaTaskInstance
import io.choerodon.asgard.saga.SagaDefinition
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Stepwise
class SagaTaskInstanceMapperSpec extends Specification {

    @Autowired
    SagaTaskInstanceMapper sagaTaskInstanceMapper

    @Shared
    SagaTaskInstance sagaTaskInstance = new SagaTaskInstance()

    @Shared
    def lock = '127.0.0.1:8090'

    def 'insert'() {
        given: '创建一个bean'
        def testCode = 'SagaTaskInstanceMapperSpec'
        def testSagaCode = 'SagaTaskInstanceMapperSpec_saga'
        sagaTaskInstance.setTaskCode(testCode)
        sagaTaskInstance.setSagaCode(testSagaCode)
        sagaTaskInstance.setSeq(1)
        sagaTaskInstance.setSagaInstanceId(1L)
        sagaTaskInstance.setMaxRetryCount(1)
        sagaTaskInstance.setTimeoutPolicy(SagaDefinition.TimeoutPolicy.RETRY.name())
        sagaTaskInstance.setTimeoutSeconds(1)
        sagaTaskInstance.setRetriedCount(0)
        sagaTaskInstance.setMaxRetryCount(1)
        sagaTaskInstance.setStatus(SagaDefinition.TaskInstanceStatus.RUNNING.name())

        when: '插入数据库'
        sagaTaskInstanceMapper.insert(sagaTaskInstance)

        then: '在数据库查询对比数据'
        sagaTaskInstance.getId() != null
        def data = sagaTaskInstanceMapper.selectByPrimaryKey(sagaTaskInstance.getId())
        data.getTaskCode() == testCode
        data.getSagaCode() == testSagaCode
        data.getSeq() == 1
        data.getSagaInstanceId() == 1L
        data.getTimeoutPolicy() == SagaDefinition.TimeoutPolicy.RETRY.name()
        data.getTimeoutSeconds() == 1
        data.getRetriedCount() == 0
        data.getMaxRetryCount() == 1
        data.getStatus() == SagaDefinition.TaskInstanceStatus.RUNNING.name()
    }

    def 'pollBatch'() {
        when: '根据code查询'
        def result = sagaTaskInstanceMapper.pollBatchNoneLimit(sagaTaskInstance.getSagaCode(), sagaTaskInstance.getTaskCode(), 'instance')

        then: '数据不为空;状态为RUNNING;instance_lock为null'
        result != null
        result.size() > 0
        def data = result.get(0)
        data.getTaskCode() == sagaTaskInstance.getTaskCode()
        data.getStatus() == SagaDefinition.TaskInstanceStatus.STATUS_RUNNING.name()
        data.getInstanceLock() == null
    }

    def 'select'() {
        when: '根据ID查询'
        def data = sagaTaskInstanceMapper.selectByPrimaryKey(sagaTaskInstance.getId())

        then: '数据ID不为空'
        data.getId() != null
    }


    def 'update'() {
        given: '更新bean数据'
        def testCode = 'SagaTaskInstanceMapperSpec_update'
        def testSagaCode = 'SagaTaskInstanceMapperSpec_saga_update'
        sagaTaskInstance.setTaskCode(testCode)
        sagaTaskInstance.setSagaCode(testSagaCode)
        sagaTaskInstance.setSeq(2)
        sagaTaskInstance.setSagaInstanceId(2L)
        sagaTaskInstance.setMaxRetryCount(2)
        sagaTaskInstance.setTimeoutPolicy(SagaDefinition.TimeoutPolicy.ALERT_ONLY.name())
        sagaTaskInstance.setTimeoutSeconds(2)
        sagaTaskInstance.setRetriedCount(1)
        sagaTaskInstance.setMaxRetryCount(2)

        when: '执行数据库更新'
        def objectVersionNumber = sagaTaskInstanceMapper.selectByPrimaryKey(sagaTaskInstance.getId()).getObjectVersionNumber()
        sagaTaskInstance.setObjectVersionNumber(objectVersionNumber)
        sagaTaskInstanceMapper.updateByPrimaryKeySelective(sagaTaskInstance)

        then: '数据库查询对比数据'
        def data = sagaTaskInstanceMapper.selectByPrimaryKey(sagaTaskInstance.getId())
        data.getTaskCode() == testCode
        data.getSagaCode() == testSagaCode
        data.getObjectVersionNumber() == objectVersionNumber + 1
        data.getSeq() == 2
        data.getSagaInstanceId() == 2L
        data.getTimeoutPolicy() == SagaDefinition.TimeoutPolicy.ALERT_ONLY.name()
        data.getTimeoutSeconds() == 2
        data.getRetriedCount() == 1
        data.getMaxRetryCount() == 2
    }


    def 'increaseRetriedCount'() {
        given: '查询数据'
        def data = sagaTaskInstanceMapper.selectByPrimaryKey(sagaTaskInstance.getId())

        when: '执行增加重试次数操作'
        sagaTaskInstanceMapper.increaseRetriedCount(sagaTaskInstance.getId())

        then: '查询数据库验证重试次数'
        def newData = sagaTaskInstanceMapper.selectByPrimaryKey(sagaTaskInstance.getId())
        newData.getObjectVersionNumber() == data.getObjectVersionNumber() + 1
        newData.getRetriedCount() == data.getRetriedCount() + 1
    }

    def 'lockByInstance'() {
        given: '查询数据'
        def data = sagaTaskInstanceMapper.selectByPrimaryKey(sagaTaskInstance.getId())


        when: '执行lockByInstance并重新查询数据'
        sagaTaskInstanceMapper.lockByInstance(data.getId(), lock)
        def newData = sagaTaskInstanceMapper.selectByPrimaryKey(sagaTaskInstance.getId())

        then: '验证实例锁已加上'
        newData.getObjectVersionNumber() == data.getObjectVersionNumber() + 1
        newData.getInstanceLock() == lock
    }

    def 'unlockByInstance'() {
        when: '执行unlockByInstance'
        sagaTaskInstanceMapper.unlockByInstance(lock)

        then: '数据库查询有无该lock的数据为空'
        SagaTaskInstance query = new SagaTaskInstance()
        query.setInstanceLock(lock)
        def lockCount = sagaTaskInstanceMapper.selectCount(query)
        lockCount == 0
    }

    def 'delete'() {
        when: '根据ID删除'
        sagaTaskInstanceMapper.deleteByPrimaryKey(sagaTaskInstance.getId())

        then: '查询数据为空'
        sagaTaskInstanceMapper.selectByPrimaryKey(sagaTaskInstance.getId()) == null
    }

}
