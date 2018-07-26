package io.choerodon.asgard.api.service.impl

import io.choerodon.asgard.IntegrationTestConfiguration
import io.choerodon.asgard.api.service.SagaTaskService
import io.choerodon.asgard.domain.SagaTask
import io.choerodon.asgard.infra.mapper.SagaTaskMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import spock.lang.Specification

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class SagaTaskServiceSpec extends Specification {

    @Autowired
    SagaTaskService sagaTaskService

    @Autowired
    SagaTaskMapper sagaTaskMapper

    def 'createSagaTaskList'() {
        given: '创建一个sagaTask bean'
        SagaTask sagaTask = new SagaTask()
        def testCode = 'createSagaTaskList'
        def testSagaCode = 'createSagaTaskList_saga'
        def service = 'createSagaTaskList_service'
        sagaTask.setCode(testCode)
        sagaTask.setSagaCode(testSagaCode)
        sagaTask.setSeq(1)
        sagaTask.setIsEnabled(true)
        sagaTask.setMaxRetryCount(1)
        sagaTask.setService(service)


        when: '调用createSagaTaskList方法创建并数据库查询'
        sagaTaskService.createSagaTaskList(Arrays.asList(sagaTask), service)

        SagaTask query = new SagaTask()
        query.setSagaCode(testSagaCode)
        query.setCode(testCode)
        def result = sagaTaskMapper.selectOne(query)

        then: '对比数据库查询数据'
        result != null
        result.getCode() == testCode
        result.getSagaCode() == testSagaCode
        result.getSeq() == 1
        result.getIsEnabled()
        result.getMaxRetryCount() == 1
        result.getService() == service
    }

}
