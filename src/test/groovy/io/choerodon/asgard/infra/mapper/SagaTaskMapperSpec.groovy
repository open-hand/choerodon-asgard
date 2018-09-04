package io.choerodon.asgard.infra.mapper

import io.choerodon.asgard.IntegrationTestConfiguration
import io.choerodon.asgard.domain.SagaTask
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import spock.lang.Specification
import spock.lang.Stepwise

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Stepwise
class SagaTaskMapperSpec extends Specification {

    @Autowired
    SagaTaskMapper sagaTaskMapper

    def '测试 插入方法'() {
        given: '创建一个对象'
        def sagaTask = new SagaTask()
        sagaTask.setCode('test_code')
        sagaTask.setSagaCode('test_saga_code')
        sagaTask.setSeq(1)
        sagaTask.setIsEnabled(true)
        sagaTask.setMaxRetryCount(1)

        when: '数据库插入数据库'
        def rowNum = sagaTaskMapper.insert(sagaTask)

        then: '根据ID在数据库查询对比数据'
        rowNum == 1
        sagaTask.getId() != null
        def data = sagaTaskMapper.selectByPrimaryKey(sagaTask.getId())
        data.getCode() == sagaTask.getCode()
        data.getSagaCode() == sagaTask.getSagaCode()
        data.getSeq() == sagaTask.getSeq()
        data.getIsEnabled() == sagaTask.getIsEnabled()
        data.getMaxRetryCount() == sagaTask.getMaxRetryCount()
        data.getObjectVersionNumber() != null
        data.getCreationDate() != null
        data.getCreatedBy() != null
    }

}
