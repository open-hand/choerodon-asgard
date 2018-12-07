package io.choerodon.asgard.api.service

import io.choerodon.asgard.IntegrationTestConfiguration
import io.choerodon.asgard.api.dto.SagaTaskInstanceStatusDTO
import io.choerodon.asgard.api.service.impl.SagaTaskInstanceServiceImpl
import io.choerodon.asgard.domain.SagaInstance
import io.choerodon.asgard.domain.SagaTaskInstance
import io.choerodon.asgard.domain.SagaTaskInstanceBuilder
import io.choerodon.asgard.domain.SagaTaskInstanceDTOBuilder
import io.choerodon.asgard.infra.mapper.JsonDataMapper
import io.choerodon.asgard.infra.mapper.SagaInstanceMapper
import io.choerodon.asgard.infra.mapper.SagaTaskInstanceMapper
import io.choerodon.asgard.infra.utils.StringLockProvider
import io.choerodon.asgard.saga.SagaDefinition
import io.choerodon.asgard.saga.dto.PollBatchDTO
import io.choerodon.asgard.saga.dto.PollCodeDTO
import io.choerodon.core.exception.CommonException
import io.choerodon.mybatis.pagehelper.domain.PageRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class SagaTaskInstanceServiceSpec extends Specification {

    @Autowired
    DataSourceTransactionManager transactionManager

    def '测试 unlockByInstance方法'() {
        given: 'mock mapper'
        def sagaTaskInstanceMapper = Mock(SagaTaskInstanceMapper)
        def sagaTaskInstanceService = new SagaTaskInstanceServiceImpl(sagaTaskInstanceMapper, null, null, null, null, null)
        def instance = '127.0.0.1:8293'

        when: '调用unlockByInstance方法'
        sagaTaskInstanceService.unlockByInstance(instance)

        then: '验证mapper调用'
        1 * sagaTaskInstanceMapper.unlockByInstance(instance)
    }

    def '测试 unlockById方法'() {
        given: '创建测试所需要的对象'
        def taskInstance = new SagaTaskInstance()

        and: 'mock mapper'
        def sagaTaskInstanceMapper = Mock(SagaTaskInstanceMapper)
        def sagaTaskInstanceService = new SagaTaskInstanceServiceImpl(sagaTaskInstanceMapper, null, null, null, null, null)

        when: '当该taskInstance不存在时调用unlockById方法'
        sagaTaskInstanceService.unlockById(10L)

        then: '抛出CommonException'
        CommonException exception = thrown CommonException
        exception.getCode() == 'error.sagaTaskInstance.notExist'

        when: '当该taskInstance存在时调用unlockById方法'
        sagaTaskInstanceMapper.selectByPrimaryKey(_) >> taskInstance
        sagaTaskInstanceService.unlockById(10L)

        then: '验证实例的instance和mapper的调用'
        taskInstance.getInstanceLock() == null
        1 * sagaTaskInstanceMapper.updateByPrimaryKey(taskInstance)
    }

    def '测试 retry方法'() {
        given: '创建测试对象'
        def sagaTaskInstance = new SagaTaskInstance()
        sagaTaskInstance.setId(10L)
        sagaTaskInstance.setSagaInstanceId(20L)
        def sagaInstance = new SagaInstance()

        and: 'mock mapper'
        def sagaTaskInstanceMapper = Mock(SagaTaskInstanceMapper) {
            selectByPrimaryKey(sagaTaskInstance.getId()) >> sagaTaskInstance
            selectByPrimaryKey(0L) >> null
        }
        def sagaInstanceMapper = Mock(SagaInstanceMapper) {
            selectByPrimaryKey(sagaTaskInstance.getSagaInstanceId()) >> sagaInstance
            selectByPrimaryKey(0L) >> null
        }
        def sagaTaskInstanceService = new SagaTaskInstanceServiceImpl(sagaTaskInstanceMapper, null, sagaInstanceMapper, null, null, null)

        when: '当该taskInstance不存在时调用retry方法'
        sagaTaskInstanceService.retry(0L)

        then: '抛出CommonException'
        CommonException exception = thrown CommonException
        exception.getCode() == 'error.sagaTaskInstance.notExist'

        when: 'taskInstance存在、instance不存在时调用retry方法'
        sagaTaskInstance.setSagaInstanceId(0L)
        sagaTaskInstanceService.retry(sagaTaskInstance.getId())

        then: '抛出CommonException'
        CommonException exception1 = thrown CommonException
        exception1.getCode() == 'error.sagaInstance.notExist'

        when: '正常调用retry方法'
        sagaTaskInstance.setSagaInstanceId(20L)
        sagaTaskInstanceService.retry(sagaTaskInstance.getId())

        then: 'mapper被执行'
        1 * sagaInstanceMapper.updateByPrimaryKey(sagaInstance)
        1 * sagaTaskInstanceMapper.updateByPrimaryKeySelective(sagaTaskInstance)
    }


    def '测试 pollBatch方法'() {
        given: '创建测试所需要的对象'
        def pollBatchDTO = new PollBatchDTO()
        pollBatchDTO.setCodes([new PollCodeDTO('saga', 'task')])
        pollBatchDTO.setInstance('test')
        pollBatchDTO.setMaxPollSize(10)
        def time = System.currentTimeMillis()
        def task1 = SagaTaskInstanceDTOBuilder.aSagaTaskInstanceDTO()
                .withId(1).withConcurrentLimitNum(1)
                .withActualEndTime(null).withObjectVersionNumber(1)
                .withRefId('id').withRefType('type')
                .withCreationDate(new Date(time).toString()).withInstanceLock('test').build()
        def task2 = SagaTaskInstanceDTOBuilder.aSagaTaskInstanceDTO()
                .withId(2).withConcurrentLimitNum(1)
                .withActualEndTime(new Date()).withObjectVersionNumber(1)
                .withRefId('id').withRefType('type')
                .withCreationDate(new Date(time + 20).toString()).withInstanceLock(null).build()
        def task3 = SagaTaskInstanceDTOBuilder.aSagaTaskInstanceDTO()
                .withId(3).withConcurrentLimitNum(1)
                .withActualEndTime(new Date()).withObjectVersionNumber(1)
                .withRefId('id').withRefType('type')
                .withCreationDate(new Date(time + 30).toString()).withInstanceLock('test').build()
        def task4 = SagaTaskInstanceDTOBuilder.aSagaTaskInstanceDTO()
                .withId(4).withConcurrentLimitNum(1)
                .withActualEndTime(new Date()).withObjectVersionNumber(1)
                .withRefId('id').withRefType('type')
                .withCreationDate(new Date(time + 40).toString()).withInstanceLock('test').build()
        def task5 = SagaTaskInstanceDTOBuilder.aSagaTaskInstanceDTO()
                .withId(5).withConcurrentLimitNum(1)
                .withActualEndTime(new Date()).withObjectVersionNumber(1)
                .withRefId('id').withRefType('type')
                .withCreationDate(new Date(time + 50).toString()).withInstanceLock('test').build()
        def task6 = SagaTaskInstanceDTOBuilder.aSagaTaskInstanceDTO()
                .withId(6).withConcurrentLimitNum(1)
                .withActualEndTime(new Date()).withObjectVersionNumber(1)
                .withRefId('id').withRefType('type')
                .withCreationDate(new Date(time + 60).toString()).withInstanceLock('test').build()

        and: 'mock mapper'
        def sagaTaskInstanceMapper = Mock(SagaTaskInstanceMapper) {
            lockByInstanceAndUpdateStartTime(_, _, _, _) >> 1
            pollBatchNoneLimit(_, _, _) >> [task1, task2]
            pollBatchTypeAndIdLimit(_, _) >> [task3, task4]
            pollBatchTypeLimit(_, _) >> [task5, task6]
        }
        def sagaTaskInstanceService = new SagaTaskInstanceServiceImpl(sagaTaskInstanceMapper, new StringLockProvider(), null, null, null, null)

        when: '执行poll方法'
        def result = sagaTaskInstanceService.pollBatch(pollBatchDTO)

        then: '验证结果'
        result.size() == 4
        result.contains(task1)
        result.contains(task2)
        result.contains(task3)
        result.contains(task5)
        !result.contains(task4)
        !result.contains(task6)
    }


    def '测试 updateStatus的更新失败方法'() {
        given: '创建测试需要的对象'
        def taskInstance = SagaTaskInstanceBuilder.aSagaTaskInstance().withSagaInstanceId(1L).withSeq(5)
                .withId(66L).withRetriedCount(10).withMaxRetryCount(10).build()
        def instance = new SagaInstance()

        def failedDto = new SagaTaskInstanceStatusDTO(10L, SagaDefinition.TaskInstanceStatus.FAILED.name(), null, 'msg')

        and: 'mock mapper'
        def mockTaskInstanceMapper = Mock(SagaTaskInstanceMapper) {
            selectByPrimaryKey(_) >> taskInstance
        }
        mockTaskInstanceMapper.updateByPrimaryKey(_) >> 1

        def mockInstanceMapper = Mock(SagaInstanceMapper) {
            selectByPrimaryKey(_) >> instance
        }

        def sagaTaskInstanceService = new SagaTaskInstanceServiceImpl(mockTaskInstanceMapper, null, mockInstanceMapper, null, transactionManager, null)

        when: '当重试次数大于等于最大重试次数调用更新失败方法'
        sagaTaskInstanceService.updateStatus(failedDto)

        then: '验证更新方法被调用'
        1 * mockInstanceMapper.updateByPrimaryKeySelective(instance)

        when: '当重试次数小于最大重试次数调用更新失败方法'
        taskInstance.setMaxRetryCount(20)
        sagaTaskInstanceService.updateStatus(failedDto)

        then: '验证增加重试次数方法被调用'
        1 * mockTaskInstanceMapper.increaseRetriedCount(_)

    }

    def '测试 updateStatus的更新成功方法'() {
        given: '创建测试需要的对象'
        def finishedTask = SagaTaskInstanceBuilder.aSagaTaskInstance().withSagaInstanceId(1L).withSeq(5)
                .withStatus(SagaDefinition.TaskInstanceStatus.COMPLETED.name()).withId(66L)
                .withRetriedCount(10).withMaxRetryCount(10).build()
        def unfinishedTask1 = SagaTaskInstanceBuilder.aSagaTaskInstance().withSagaInstanceId(1L).withSeq(5)
                .withStatus(SagaDefinition.TaskInstanceStatus.RUNNING.name()).withId(66L).withRetriedCount(10)
                .withMaxRetryCount(10).build()
        def unfinishedTask2 = SagaTaskInstanceBuilder.aSagaTaskInstance().withSagaInstanceId(1L).withSeq(10)
                .withStatus(SagaDefinition.TaskInstanceStatus.RUNNING.name()).withId(66L).withRetriedCount(10)
                .withMaxRetryCount(10).build()
        def taskInstance = SagaTaskInstanceBuilder.aSagaTaskInstance().withSagaInstanceId(1L).withSeq(5)
                .withId(66L).withRetriedCount(10).withMaxRetryCount(10).build()
        def instance = new SagaInstance()

        def completeDto = new SagaTaskInstanceStatusDTO(10L, SagaDefinition.TaskInstanceStatus.COMPLETED.name(), 'out', null)

        and: 'mock mapper'
        def mockUpdateTaskInstanceMapper = Mock(SagaTaskInstanceMapper) {
            selectByPrimaryKey(_) >> taskInstance
        }
        mockUpdateTaskInstanceMapper.updateByPrimaryKeySelective(_) >> 1

        def mockInstanceMapper = Mock(SagaInstanceMapper) {
            selectByPrimaryKey(_) >> instance
        }
        def mockJsonDataMapper = Mock(JsonDataMapper) {
            insertSelective(_) >> 1
        }
        def sagaTaskInstanceService = new SagaTaskInstanceServiceImpl(mockUpdateTaskInstanceMapper, null, mockInstanceMapper, mockJsonDataMapper, transactionManager, null)

        when: '当当前层级还有未完成消息时调用更新状态成功方法'
        mockUpdateTaskInstanceMapper.select(_) >> [unfinishedTask1]
        sagaTaskInstanceService.updateStatus(completeDto)

        then: 'instance的更新方法未被调用'
        0 * mockInstanceMapper.updateByPrimaryKeySelective(instance)

        when: '当当前层级没有未完成消息、下一层级有未完成消息时调用更新状态成功方法'
        mockUpdateTaskInstanceMapper.select(_) >> [finishedTask, unfinishedTask2]
        sagaTaskInstanceService.updateStatus(completeDto)

        then: 'instance的更新方法未被调用'
        0 * mockInstanceMapper.updateByPrimaryKeySelective(instance)

        when: '当当前层级没有未完成消息，没有下一层级的时调用更新状态成功方法'
        mockUpdateTaskInstanceMapper.select(_) >> [finishedTask]
        sagaTaskInstanceService.updateStatus(completeDto)

        then: '验证taskInstance的更新方法被调用被调用一次;instance的更新方法被调用一次'
        1 * mockInstanceMapper.updateByPrimaryKeySelective(instance)
    }

    def '测试 pageQuery方法'() {
        given: 'mock mapper'
        def sagaTaskInstanceMapper = Mock(SagaTaskInstanceMapper)
        def sagaTaskInstanceService = new SagaTaskInstanceServiceImpl(sagaTaskInstanceMapper, null, null, null, null, null)

        when: '调用pageQuery方法'
        sagaTaskInstanceService.pageQuery(new PageRequest(), "", "", "", "", "site", 0L)

        then: '验证SagaInstanceMapper的fulltextSearch方法被调用'
        1 * sagaTaskInstanceMapper.fulltextSearchTaskInstance(_, _, _, _, _, _)
    }

}
