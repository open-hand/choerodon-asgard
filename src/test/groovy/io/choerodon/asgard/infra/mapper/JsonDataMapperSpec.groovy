package io.choerodon.asgard.infra.mapper

import io.choerodon.asgard.IntegrationTestConfiguration
import io.choerodon.asgard.domain.JsonData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.Transactional
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Transactional
class JsonDataMapperSpec extends Specification {

    @Autowired
    JsonDataMapper jsonDataMapper

    def '测试 插入方法'() {
        given: '创建一个对象'
        def jsonData = new JsonData()
        jsonData.setData('test-data')

        when: '调用mapper的插入方法'
        def rowNum = jsonDataMapper.insert(jsonData)

        then: '数据库查询确认插入'
        rowNum == 1
        jsonData.getId() != null
        def data = jsonDataMapper.selectByPrimaryKey(jsonData.getId())
        data != null
        data.getData() == jsonData.getData()
        data.getObjectVersionNumber() != null
        data.getCreationDate() != null
        data.getCreatedBy() != null
    }

}
