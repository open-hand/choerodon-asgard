package io.choerodon.asgard.infra.mapper

import io.choerodon.asgard.IntegrationTestConfiguration
import io.choerodon.asgard.domain.JsonData
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
class JsonDataMapperSpec extends Specification {

    @Autowired
    JsonDataMapper jsonDataMapper

    @Shared
    JsonData jsonData = new JsonData()

    def 'insert'() {
        given: '创建一个bean'
        def testData = 'test_data'
        jsonData.setData(testData)

        when: '插入数据库'
        jsonDataMapper.insert(jsonData)

        then: '返回ID'
        jsonData.getId() != null

        when: '根据ID在数据库查询'
        def data = jsonDataMapper.selectByPrimaryKey(jsonData.getId())

        then: '对比数据'
        data.getData() == testData
    }

    def 'update'() {
        given: '更新bean数据'
        def testData = 'test_update_data'
        jsonData.setData(testData)

        when: '执行数据库更新,未传入objectVersionNumber,更新失败'
        def rowNum = jsonDataMapper.updateByPrimaryKeySelective(jsonData)

        then: '更新行数为0'
        rowNum == 0

        when: '执行数据库更新,传入objectVersionNumber'
        def objectVersionNumber = jsonDataMapper.selectByPrimaryKey(jsonData.getId()).getObjectVersionNumber()
        jsonData.setObjectVersionNumber(objectVersionNumber)
        rowNum = jsonDataMapper.updateByPrimaryKeySelective(jsonData)

        then: '对比数据'
        def data = jsonDataMapper.selectByPrimaryKey(jsonData.getId())
        rowNum == 1
        data.getData() == testData
        data.getObjectVersionNumber() == objectVersionNumber + 1
    }

    def 'select'() {
        when: '根据ID查询'
        def data = jsonDataMapper.selectByPrimaryKey(jsonData.getId())

        then: '数据ID不为空'
        data.getId() != null
    }

    def 'delete'() {
        when: '根据ID删除'
        jsonDataMapper.deleteByPrimaryKey(jsonData.getId())

        then: '查询数据为空'
        jsonDataMapper.selectByPrimaryKey(jsonData.getId()) == null
    }

}
