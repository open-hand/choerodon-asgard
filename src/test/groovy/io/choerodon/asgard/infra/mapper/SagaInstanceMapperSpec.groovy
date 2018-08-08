package io.choerodon.asgard.infra.mapper

import io.choerodon.asgard.IntegrationTestConfiguration
import io.choerodon.asgard.domain.SagaInstance
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
class SagaInstanceMapperSpec extends Specification {

    @Autowired
    SagaInstanceMapper sagaInstanceMapper

    @Shared
    SagaInstance sagaInstance = new SagaInstance()

    def 'insert'() {
        given: '创建一个bean'
        def testCode = 'test-code'
        sagaInstance.setSagaCode(testCode)
        sagaInstance.setStatus(SagaDefinition.TaskInstanceStatus.RUNNING.name())

        when: '插入数据库'
        sagaInstanceMapper.insert(sagaInstance)

        then: '返回ID'
        sagaInstance.getId() != null

        when: '根据ID在数据库查询'
        def data = sagaInstanceMapper.selectByPrimaryKey(sagaInstance.getId())

        then: '对比数据'
        data.getStatus() == SagaDefinition.TaskInstanceStatus.RUNNING.name()
        data.getSagaCode() == testCode
    }

    def 'update'() {
        given: '更新bean数据'
        def testCode = 'test_update_code'
        sagaInstance.setSagaCode(testCode)
        sagaInstance.setStatus(SagaDefinition.TaskInstanceStatus.COMPLETED.name())

        when: '执行数据库更新'
        def objectVersionNumber = sagaInstanceMapper.selectByPrimaryKey(sagaInstance.getId()).getObjectVersionNumber()
        sagaInstance.setObjectVersionNumber(objectVersionNumber)
        sagaInstanceMapper.updateByPrimaryKeySelective(sagaInstance)

        then: '对比数据'
        def data = sagaInstanceMapper.selectByPrimaryKey(sagaInstance.getId())
        data.getStatus() == SagaDefinition.TaskInstanceStatus.COMPLETED.name()
        data.getSagaCode() == testCode
        data.getObjectVersionNumber() == objectVersionNumber + 1
    }

    def 'select'() {
        when: '根据ID查询'
        def data = sagaInstanceMapper.selectByPrimaryKey(sagaInstance.getId())

        then: '数据ID不为空'
        data.getId() != null
    }

    def 'delete'() {
        when: '根据ID删除]'
        sagaInstanceMapper.deleteByPrimaryKey(sagaInstance.getId())

        then: '查询数据为空'
        sagaInstanceMapper.selectByPrimaryKey(sagaInstance.getId()) == null
    }

}
