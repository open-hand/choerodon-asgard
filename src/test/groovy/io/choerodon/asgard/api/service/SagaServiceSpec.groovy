package io.choerodon.asgard.api.service

import io.choerodon.asgard.IntegrationTestConfiguration
import io.choerodon.asgard.api.service.impl.SagaServiceImpl
import io.choerodon.asgard.domain.Saga
import io.choerodon.asgard.domain.SagaTask
import io.choerodon.asgard.infra.mapper.SagaMapper
import io.choerodon.asgard.infra.mapper.SagaTaskMapper
import io.choerodon.core.exception.CommonException
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class SagaServiceSpec extends Specification {

    def '测试 create方法'() {
        given: '创建测试saga对象'
        def saga = new Saga(null, 'iam', 'test', '{}', 'test')

        and: 'mock一个sagaMapper'
        def mockSagaMapper = Mock(SagaMapper)
        def sagaService = new SagaServiceImpl(mockSagaMapper, null)

        when: '使用不合法的Saga调用create方法'
        sagaService.create(saga)

        then: 'selectOne未被执行'
        0 * mockSagaMapper.selectOne(_)

        when: '使用合法的Saga调用create方法'
        saga.setCode('code')
        sagaService.create(saga)

        then: 'selectOne和insertSelective被执行'
        1 * mockSagaMapper.selectOne(_)
        1 * mockSagaMapper.insertSelective(_)

        when: 'selectOne返回不为null'
        mockSagaMapper.selectOne(_) >> saga
        sagaService.create(saga)

        then: 'updateByPrimaryKeySelective被执行'
        1 * mockSagaMapper.updateByPrimaryKeySelective(_)
    }

    def '测试 pagingQuery方法'() {
        given: 'mock一个sagaMapper'
        def mockSagaMapper = Mock(SagaMapper)
        def sagaService = new SagaServiceImpl(mockSagaMapper, null)

        when: '调用pagingQuery方法'
        sagaService.pagingQuery(1, 20, '', '', '', '')

        then: 'sagaMapper的fulltextSearch方法被调用'
        1 * mockSagaMapper.fulltextSearch(_, _, _, _)
    }

    def '测试 query方法'() {
        given: '创建测试数据'
        def saga = new Saga('saga', 'iam', 'desc', '{}', 'test')
        def task1 = new SagaTask('one', 'saga', 2, true, 'iam')
        def task2 = new SagaTask('two', 'saga', 6, true, 'iam')
        def task3 = new SagaTask('three', 'saga', 6, true, 'iam')
        def tasks = [task1, task2, task3]

        and: 'mock一个sagaMapper'
        def mockSagaMapper = Stub(SagaMapper)
        mockSagaMapper.selectByPrimaryKey(_) >> saga
        def mockSagaMapperSelectNull = Stub(SagaMapper)
        mockSagaMapperSelectNull.selectByPrimaryKey(_) >> null
        def mockSagaTaskMapper = Stub(SagaTaskMapper)
        mockSagaTaskMapper.select(_) >> tasks
        def sagaService = new SagaServiceImpl(mockSagaMapperSelectNull, mockSagaTaskMapper)

        when: '查询的saga不存在'
        sagaService.query(99L)

        then: '抛出commonException'
        CommonException notExist = thrown CommonException
        notExist.getCode() == 'error.saga.notExist'

        when: '查询的saga不存在且存在三个task'
        sagaService.setSagaMapper(mockSagaMapper)
        def result = sagaService.query(99L).getBody()

        then: '验证查询结果'
        result != null
        result.getCode() == saga.getCode()
        result.getTasks().size() == 2
        result.getTasks().get(0).size() == 1
        result.getTasks().get(0).get(0).getCode() == task1.getCode()
        result.getTasks().get(1).size() == 2
        result.getTasks().get(1).get(0).getSeq() == task2.getSeq()
    }

    def '测试 delete方法'() {
        given: 'mock一个sagaMapper'
        def mockSagaMapper = Mock(SagaMapper)
        def mockSagaTaskMapper = Mock(SagaTaskMapper)
        def sagaService = new SagaServiceImpl(mockSagaMapper, mockSagaTaskMapper)

        when: '当删除的saga不存在'
        sagaService.delete(1L)

        then: '抛出commonException'
        CommonException notExist = thrown CommonException
        notExist.getCode() == 'error.saga.notExist'

        when: '当删除的saga存在且不存在task'
        mockSagaMapper.selectByPrimaryKey(_) >> new Saga('code')
        mockSagaTaskMapper.select(_) >> []
        sagaService.delete(1L)

        then: 'deleteByPrimaryKey执行'
        1 * mockSagaMapper.deleteByPrimaryKey(_)

        when: '当删除的saga存在且存在task'
        mockSagaTaskMapper.select(_) >> [new SagaTask()]
        mockSagaMapper.selectByPrimaryKey(_) >> new Saga('code')
        sagaService.delete(1L)

        then: '抛出commonException'
        CommonException existTask = thrown CommonException
        existTask.getCode() == 'error.saga.deleteWhenTaskExist'
    }

}
