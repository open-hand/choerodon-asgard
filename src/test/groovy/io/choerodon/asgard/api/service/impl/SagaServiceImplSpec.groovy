package io.choerodon.asgard.api.service.impl

import io.choerodon.asgard.IntegrationTestConfiguration
import io.choerodon.asgard.api.service.SagaService
import io.choerodon.asgard.domain.Saga
import io.choerodon.asgard.infra.mapper.SagaMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import spock.lang.Specification

/**
 * Created by hailuoliu@choerodon.io on 2018/7/14.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class SagaServiceImplSpec extends Specification {

    @Autowired
    SagaService sagaService

    @Autowired
    SagaMapper sagaMapper

    def 'createSaga'() {
        given: '创建一个saga bean'
        Saga saga = new Saga()
        saga.code = 'CreateSaga'
        saga.service = 'CreateSaga-service'

        when: '调用SagaService的创建方法'
        sagaService.createSaga(saga)

        then: '数据库查询该saga已经存在'
        sagaMapper.existByCode(saga.getCode())

    }
}
