package io.choerodon.asgard.infra.mapper

import io.choerodon.asgard.IntegrationTestConfiguration
import io.choerodon.asgard.domain.Saga
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.Transactional
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Transactional
class SagaMapperSpec extends Specification {

    @Autowired
    SagaMapper sagaMapper

    def '测试 插入方法'() {
        given: '创建一个对象'
        def saga = new Saga()
        saga.setCode('test-saga')

        when: '调用mapper的插入方法'
        def rowNum = sagaMapper.insert(saga)

        then: '数据库查询确认插入'
        rowNum == 1
        saga.getId() != null
        def data = sagaMapper.selectByPrimaryKey(saga.getId())
        data.getCode() == saga.getCode()
        data.getObjectVersionNumber() != null
        data.getCreationDate() != null
        data.getCreatedBy() != null
    }

    def '测试 existByCode方法'() {
        given: '数据库插入一条测试数据'
        def saga = new Saga()
        saga.setCode('existByCode')
        sagaMapper.insert(saga)

        when: '调用existByCode方法'
        def exist = sagaMapper.existByCode(saga.getCode())

        then: '验证存在'
        exist
    }


    def '测试 fulltextSearch方法'() {
        given: '准备查询数据'
        def dbData = new Saga('fs_saga', 'asgard-service', 'saga测试code', '{}', 'INPUT_SCHEMA')
        sagaMapper.insert(dbData)

        expect: '期望的结果数量'
        sagaMapper.fulltextSearch(code, description, service, params).size() == size

        where: '验证查询结果数量'
        code       || description || service  || params     || size
        'fs_saga'  || null        || null     || null       || 1
        null       || 'saga测试'    || null     || null       || 1
        null       || null        || 'asgard' || null       || 1
        'fs_saga1' || null        || null     || null       || 0
        null       || null        || null     || 'fs_saga'  || 1
        null       || null        || null     || 'saga测试'   || 1
        null       || null        || null     || 'asgard'   || 1
        null       || null        || null     || 'fs_saga1' || 0
        null       || null        || null     || 'saga测试1'  || 0
    }

}
