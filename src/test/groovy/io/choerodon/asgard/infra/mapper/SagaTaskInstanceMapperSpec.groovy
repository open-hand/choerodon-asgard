package io.choerodon.asgard.infra.mapper

import io.choerodon.asgard.IntegrationTestConfiguration
import io.choerodon.asgard.api.dto.SagaTaskInstanceDTO
import io.choerodon.asgard.domain.SagaTaskInstanceBuilder
import io.choerodon.asgard.saga.SagaDefinition
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.Transactional
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Transactional
class SagaTaskInstanceMapperSpec extends Specification {

    @Autowired
    SagaTaskInstanceMapper sagaTaskInstanceMapper

    def createSagaTaskInstance(String taskCode, String sagaCode, String cp = SagaDefinition.ConcurrentLimitPolicy.NONE.name(), int cn = 1) {
        return SagaTaskInstanceBuilder.aSagaTaskInstance()
                .withTaskCode(taskCode)
                .withSagaCode(sagaCode)
                .withSeq(1)
                .withSagaInstanceId(1L)
                .withMaxRetryCount(1)
                .withTimeoutPolicy(SagaDefinition.TimeoutPolicy.RETRY.name())
                .withTimeoutSeconds(1).withRetriedCount(0)
                .withMaxRetryCount(1)
                .withConcurrentLimitPolicy(cp)
                .withConcurrentLimitNum(cn)
                .withStatus(SagaDefinition.TaskInstanceStatus.RUNNING.name()).build()
    }

    def '测试 pollBatchNoneLimit方法'() {
        given: '插入三条测试数据'
        def instance = '127.0.0.1:8080'
        def sagaTaskInstance = createSagaTaskInstance('pollNoneTask', 'pollNoneSaga')
        sagaTaskInstance.setInstanceLock(null)
        sagaTaskInstanceMapper.insert(sagaTaskInstance)
        sagaTaskInstance.setId(null)
        sagaTaskInstance.setInstanceLock(instance)
        sagaTaskInstanceMapper.insert(sagaTaskInstance)
        sagaTaskInstance.setId(null)
        sagaTaskInstance.setStatus(SagaDefinition.TaskInstanceStatus.COMPLETED.name())
        sagaTaskInstanceMapper.insert(sagaTaskInstance)

        when: '执行pollBatchNoneLimit查询'
        def result = sagaTaskInstanceMapper.pollBatchNoneLimit(sagaTaskInstance.getSagaCode(), sagaTaskInstance.getTaskCode(), instance)

        then: '查询数据应为两条；第一条id应小于第二条; 实例锁为空或为传入的实例'
        result.size() == 2
        result.get(0).getInstanceLock() == null || result.get(0).getInstanceLock() == instance
        result.get(1).getInstanceLock() == null || result.get(1).getInstanceLock() == instance
        result.get(0).getId() < result.get(1).getId()
    }

    def '测试 pollBatchTypeAndIdLimit方法'() {
        given: '插入三条测试数据'
        def instance = '127.0.0.1:8080'
        def sagaTaskInstance = createSagaTaskInstance('pollTypeIdTask', 'pollTypeIdSaga', SagaDefinition.ConcurrentLimitPolicy.TYPE_AND_ID.name())
        sagaTaskInstance.setInstanceLock(null)
        sagaTaskInstanceMapper.insert(sagaTaskInstance)
        sagaTaskInstance.setId(null)
        sagaTaskInstance.setInstanceLock(instance)
        sagaTaskInstanceMapper.insert(sagaTaskInstance)
        sagaTaskInstance.setId(null)
        sagaTaskInstance.setStatus(SagaDefinition.TaskInstanceStatus.COMPLETED.name())
        sagaTaskInstanceMapper.insert(sagaTaskInstance)

        when: '执行pollBatchTypeAndIdLimit查询'
        def result = sagaTaskInstanceMapper.pollBatchTypeAndIdLimit(sagaTaskInstance.getSagaCode(), sagaTaskInstance.getTaskCode())

        then: '查询数据应为两条; 第一条id应小于第二条'
        result.size() == 2
        result.get(0).getId() < result.get(1).getId()
    }

    def '测试 pollBatchTypeLimit方法'() {
        given: '插入三条测试数据'
        def instance = '127.0.0.1:8080'
        def sagaTaskInstance = createSagaTaskInstance('pollNoneTask', 'pollNoneSaga', SagaDefinition.ConcurrentLimitPolicy.TYPE.name())
        sagaTaskInstance.setInstanceLock(null)
        sagaTaskInstanceMapper.insert(sagaTaskInstance)
        sagaTaskInstance.setId(null)
        sagaTaskInstance.setInstanceLock(instance)
        sagaTaskInstanceMapper.insert(sagaTaskInstance)
        sagaTaskInstance.setId(null)
        sagaTaskInstance.setStatus(SagaDefinition.TaskInstanceStatus.COMPLETED.name())
        sagaTaskInstanceMapper.insert(sagaTaskInstance)

        when: '执行pollBatchTypeLimit查询'
        def result = sagaTaskInstanceMapper.pollBatchTypeLimit(sagaTaskInstance.getSagaCode(), sagaTaskInstance.getTaskCode())

        then: '查询数据应为两条；第一条id应小于第二条'
        result.size() == 2
        result.get(0).getId() < result.get(1).getId()
    }


    def '测试 increaseRetriedCount方法'() {
        given: '插入一条测试数据'
        def sagaTaskInstance = createSagaTaskInstance('rcTask', 'rcCode')
        sagaTaskInstanceMapper.insert(sagaTaskInstance)

        when: '执行增加重试次数操作'
        sagaTaskInstanceMapper.increaseRetriedCount(sagaTaskInstance.getId())

        then: '查询数据库验证重试次数'
        def newData = sagaTaskInstanceMapper.selectByPrimaryKey(sagaTaskInstance.getId())
        newData.getRetriedCount() == sagaTaskInstance.getRetriedCount() + 1
    }

    def '测试 lockByInstanceAndUpdateStartTime方法'() {
        given: '数据库插入两条测试数据'
        def sagaTaskInstance = createSagaTaskInstance('lockTask', 'lockSaga')
        sagaTaskInstance.setInstanceLock(null)
        sagaTaskInstanceMapper.insert(sagaTaskInstance)
        def db = sagaTaskInstanceMapper.selectByPrimaryKey(sagaTaskInstance.getId())


        when: '正确参数调用lockByInstanceAndUpdateStartTime方法'
        def date = new Date()
        def instance = '127.0.0.1:8080'
        def rowNum = sagaTaskInstanceMapper.lockByInstanceAndUpdateStartTime(db.getId(), instance, db.getObjectVersionNumber(), date)
        then: '影响行数为1；instance和date更新生效'
        rowNum == 1
        def db2 = sagaTaskInstanceMapper.selectByPrimaryKey(sagaTaskInstance.getId())
        db2.getInstanceLock() == instance
        db2.getActualStartTime() == date

        when: '使用错误的versionNumber调用lockByInstanceAndUpdateStartTime方法'
        def rowNum2 = sagaTaskInstanceMapper.lockByInstanceAndUpdateStartTime(db.getId(), '127.0.0.1:9090', 1000L, new Date())
        then: '影响行数为0'
        rowNum2 == 0
    }

    def '测试 unlockByInstance方法'() {
        given: '插入两条测试数据'
        def instance = '127.0.0.1:9092'
        def sagaTaskInstance = createSagaTaskInstance('unlockTask', 'unlockSaga')
        sagaTaskInstance.setInstanceLock(instance)
        sagaTaskInstance.setInstanceLock(instance)
        sagaTaskInstanceMapper.insert(sagaTaskInstance)
        def id1 = sagaTaskInstance.getId()
        sagaTaskInstance.setId(null)
        sagaTaskInstance.setStatus(SagaDefinition.TaskInstanceStatus.COMPLETED.name())
        sagaTaskInstanceMapper.insert(sagaTaskInstance)
        def id2 = sagaTaskInstance.getId()

        when: '执行unlockByInstance方法'
        int rowNum = sagaTaskInstanceMapper.unlockByInstance(instance)

        then: '数据库查询有无该lock的数据为空'
        rowNum == 1
        sagaTaskInstanceMapper.selectByPrimaryKey(id1).getInstanceLock() == null
        sagaTaskInstanceMapper.selectByPrimaryKey(id2).getInstanceLock() == instance
    }

    def '测试 selectAllBySagaInstanceId方法'() {
        given: '插入两条测试数据'
        def sagaInstanceId = 99L
        def sagaTaskInstance = createSagaTaskInstance('selectBySagaInstanceTask', 'selectBySagaInstanceSaga')
        sagaTaskInstance.setInstanceLock(null)
        sagaTaskInstance.setSagaInstanceId(sagaInstanceId)
        sagaTaskInstanceMapper.insert(sagaTaskInstance)
        sagaTaskInstance.setId(null)
        sagaTaskInstanceMapper.insert(sagaTaskInstance)

        when: '执行selectAllBySagaInstanceI方法'
        List<SagaTaskInstanceDTO> result = sagaTaskInstanceMapper.selectAllBySagaInstanceId(sagaInstanceId)

        then: '验证查询结果，条数为2'
        result.size() == 2
        result.get(0).getSagaInstanceId() == sagaInstanceId
        result.get(1).getSagaInstanceId() == sagaInstanceId
    }

}
