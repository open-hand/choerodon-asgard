package io.choerodon.asgard.infra.mapper

import io.choerodon.asgard.IntegrationTestConfiguration
import io.choerodon.asgard.domain.SagaInstance
import io.choerodon.asgard.saga.SagaDefinition
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.Transactional
import spock.lang.Specification
import spock.lang.Unroll

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Transactional
class SagaInstanceMapperSpec extends Specification {

    @Autowired
    SagaInstanceMapper sagaInstanceMapper

    def '测试 插入方法'() {
        given: '创建一个对象'
        def sagaInstance = new SagaInstance()
        sagaInstance.setSagaCode('test-code')
        sagaInstance.setSourceId(0L)
        sagaInstance.setLevel("site")
        sagaInstance.setStatus(SagaDefinition.TaskInstanceStatus.RUNNING.name())

        when: '调用mapper的插入方法'
        def rowNum = sagaInstanceMapper.insert(sagaInstance)

        then: '数据库查询确认插入'
        rowNum == 1
        sagaInstance.getId() != null
        def data = sagaInstanceMapper.selectByPrimaryKey(sagaInstance.getId())
        data.getStatus() == SagaDefinition.TaskInstanceStatus.RUNNING.name()
        data.getSagaCode() == sagaInstance.getSagaCode()
        data.getObjectVersionNumber() != null
        data.getCreationDate() != null
        data.getCreatedBy() != null
        data.getSourceId() == sagaInstance.getSourceId()
        data.getLevel() == sagaInstance.getLevel()
    }

    @Unroll
    def '测试 fulltextSearch方法'() {
        given: '准备查询数据'
        def dbData = new SagaInstance('fs_code', 'fs_type', 'fs_id', 'fs_status', new Date(), new Date())
        dbData.setSourceId(0L)
        dbData.setLevel("site")
        sagaInstanceMapper.insert(dbData)

        expect: '期望的结果数量'
        sagaInstanceMapper.fulltextSearchInstance(sagaCode, status, refType, refId, params, "site", 0L).size() == size

        where: '验证查询结果数量'
        sagaCode   || status      || refType   || refId   || params      || size
        'fs_code'  || null        || null      || null    || null        || 1
        null       || 'fs_status' || null      || null    || null        || 1
        null       || null        || 'fs_type' || null    || null        || 1
        null       || null        || null      || 'fs_id' || null        || 1
        'fs_code1' || null        || null      || null    || null        || 0
        null       || null        || null      || null    || 'fs_code'   || 1
        null       || null        || null      || null    || 'fs_type'   || 1
        null       || null        || null      || null    || 'fs_id'     || 1
        null       || null        || null      || null    || 'fs_status' || 1
        null       || null        || null      || null    || 'fs_test'   || 0
    }

}
