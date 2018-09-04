package io.choerodon.asgard.api.service

import io.choerodon.asgard.IntegrationTestConfiguration
import io.choerodon.asgard.api.service.impl.SagaTaskServiceImpl
import io.choerodon.asgard.domain.SagaTask
import io.choerodon.asgard.infra.mapper.SagaTaskMapper
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class SagaTaskServiceSpec extends Specification {

    def '测试 createSagaTaskList方法'() {
        given: '创建测试方法的参数'
        def task1 = new SagaTask('code1', 'saga', 2, true, 'iam')
        def task2 = new SagaTask('code2', 'saga', 2, true, 'iam')
        def task3 = new SagaTask(null, 'saga', 2, true, 'iam')
        def createTasks = [task1, task2, task3]
        def task4 = new SagaTask('code3', 'saga', 2, true, 'iam')
        def dbTasks = [task1, task4]

        and: 'mock mapper'
        def sagaTaskMapper = Mock(SagaTaskMapper) {
            select(_) >> dbTasks
        }
        def sagaTaskService = new SagaTaskServiceImpl(sagaTaskMapper)

        when: '调用createSagaTaskList方法'
        sagaTaskService.createSagaTaskList(createTasks, 'iam')

        then: '验证mapper方法调用'
        0 * sagaTaskMapper.insertSelective(task3)
        0 * sagaTaskMapper.updateByPrimaryKeySelective(task3)
        1 * sagaTaskMapper.insertSelective(task2)
        1 * sagaTaskMapper.updateByPrimaryKeySelective(task1)
        1 * sagaTaskMapper.updateByPrimaryKeySelective(task4)
        !task4.getIsEnabled()
    }

}
