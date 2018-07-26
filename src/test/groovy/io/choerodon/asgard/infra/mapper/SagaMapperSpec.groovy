package io.choerodon.asgard.infra.mapper

import io.choerodon.asgard.IntegrationTestConfiguration
import io.choerodon.asgard.domain.Saga
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
class SagaMapperSpec extends Specification {

    @Autowired
    SagaMapper sagaMapper

    @Shared Saga saga = new Saga()

    def 'insert'() {
        given: '创建一个bean'
        def testCode = 'test_data'
        saga.setCode(testCode)

        when: '插入数据库'
        sagaMapper.insert(saga)

        then: '返回ID'
        saga.getId() != null

        when: '根据ID在数据库查询'
        def data = sagaMapper.selectByPrimaryKey(saga.getId())

        then: '对比数据'
        data.getCode() == testCode
    }

    def 'existByCode'() {
        when: '根据code查询是否存在'
        def num = sagaMapper.existByCode(saga.getCode())

        then: '返回ID'
        num
    }

    def 'update'() {
        given: '更新bean数据'
        def testCode = 'test_update_code'
        saga.setCode(testCode)

        when: '执行数据库更新'
        def objectVersionNumber = sagaMapper.selectByPrimaryKey(saga.getId()).getObjectVersionNumber()
        saga.setObjectVersionNumber(objectVersionNumber)
        sagaMapper.updateByPrimaryKeySelective(saga)

        then: '对比数据'
        def data = sagaMapper.selectByPrimaryKey(saga.getId())
        data.getCode() == testCode
        data.getObjectVersionNumber() == objectVersionNumber + 1
    }

    def 'select'() {
        when: '根据ID查询'
        def data = sagaMapper.selectByPrimaryKey(saga.getId())

        then: '数据ID不为空'
        data.getId() != null
    }

    def 'delete'() {
        when: '根据ID删除'
        sagaMapper.deleteByPrimaryKey(saga.getId())

        then: '查询数据为空'
        sagaMapper.selectByPrimaryKey(saga.getId()) == null
    }

}
