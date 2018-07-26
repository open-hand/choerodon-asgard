package io.choerodon.asgard.infra.mapper

import io.choerodon.asgard.IntegrationTestConfiguration
import io.choerodon.asgard.domain.SagaTask
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
class SagaTaskMapperSpec extends Specification {

    @Autowired
    SagaTaskMapper sagaTaskMapper

    @Shared
    SagaTask sagaTask = new SagaTask()

    def 'insert'() {
        given: '创建一个bean'
        def testCode = 'test_code'
        def testSagaCode = 'test_saga_code'
        sagaTask.setCode(testCode)
        sagaTask.setSagaCode(testSagaCode)
        sagaTask.setSeq(1)
        sagaTask.setIsEnabled(true)
        sagaTask.setMaxRetryCount(1)

        when: '插入数据库'
        sagaTaskMapper.insert(sagaTask)

        then: '返回ID'
        sagaTask.getId() != null

        when: '根据ID在数据库查询'
        def data = sagaTaskMapper.selectByPrimaryKey(sagaTask.getId())

        then: '对比数据'
        data.getCode() == testCode
        data.getSagaCode() == testSagaCode
        data.getSeq() == 1
        data.getIsEnabled()
        data.getMaxRetryCount() == 1
    }


    def 'update'() {
        given: '更新bean数据'
        def testCode = 'test_update_code'
        def testSagaCode = 'test_update_saga_code'
        sagaTask.setSeq(2)
        sagaTask.setIsEnabled(false)
        sagaTask.setMaxRetryCount(2)
        sagaTask.setCode(testCode)
        sagaTask.setSagaCode(testSagaCode)

        when: '执行数据库更新并根据ID在数据库查询'
        def objectVersionNumber = sagaTaskMapper.selectByPrimaryKey(sagaTask.getId()).getObjectVersionNumber()
        sagaTask.setObjectVersionNumber(objectVersionNumber)
        sagaTaskMapper.updateByPrimaryKeySelective(sagaTask)

        then: '对比数据'
        def data = sagaTaskMapper.selectByPrimaryKey(sagaTask.getId())
        data.getCode() == testCode
        data.getSagaCode() == testSagaCode
        data.getSeq() == 2
        !data.getIsEnabled()
        data.getMaxRetryCount() == 2
        data.getObjectVersionNumber() == objectVersionNumber + 1
    }

    def 'select'() {
        when: '根据ID查询'
        def data = sagaTaskMapper.selectByPrimaryKey(sagaTask.getId())

        then: '数据ID不为空'
        data.getId() != null
    }

    def 'delete'() {
        when: '根据ID删除并根据ID查询'
        sagaTaskMapper.deleteByPrimaryKey(sagaTask.getId())

        then: '查询数据为空'
        sagaTaskMapper.selectByPrimaryKey(sagaTask.getId()) == null
    }


}
