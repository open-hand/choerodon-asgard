package io.choerodon.asgard.infra.utils

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.choerodon.asgard.IntegrationTestConfiguration
import io.choerodon.asgard.api.dto.JsonMergeDTO
import io.choerodon.asgard.domain.JsonData
import io.choerodon.asgard.domain.SagaTaskInstance
import io.choerodon.asgard.infra.mapper.JsonDataMapper
import io.choerodon.core.saga.SagaDefinition
import io.choerodon.swagger.property.PropertyData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class ConvertUtilsSpec extends Specification {

    @Autowired
    JsonDataMapper jsonDataMapper

    def 'convertSaga'() {
        given: '创建一个PropertyData.Saga'
        String[] input = ['name', 'id']
        String[] output = ['id', 'code', 'description']
        def data = new PropertyData.Saga('code', 'desc', input, output)
        def service = 'convertSaga'

        when: '调用ConvertUtils的convertSaga方法'
        def saga = ConvertUtils.convertSaga(data, service)

        then: '验证转换结果'
        saga.getService() == service
        saga.getInputSchema() == 'name,id'
        saga.getOutputKeys() == 'id,code,description'
        saga.getCode() == data.getCode()
        saga.getDescription() == data.getDescription()
    }

    def 'convertSagaTask'() {
        given: '创建一个PropertyData.SagaTask'
        def data = new PropertyData.SagaTask('code', 'desc', 'sagaCode', 20, 33)
        data.setTimeoutSeconds(10)
        data.setTimeoutPolicy(SagaDefinition.TimeoutPolicy.ALERT_ONLY.name())
        data.setConcurrentExecLimit(10)
        def service = 'convertSaga'

        when: '调用ConvertUtils的convertSaga方法'
        def saga = ConvertUtils.convertSagaTask(data, service)

        then: '验证转换结果'
        saga.getService() == service
        saga.getCode() == data.getCode()
        saga.getDescription() == data.getDescription()
        saga.getSagaCode() == data.getSagaCode()
        saga.getSeq() == data.getSeq()
        saga.getMaxRetryCount() == data.getMaxRetryCount()
        saga.getTimeoutSeconds() == data.getTimeoutSeconds()
        saga.getTimeoutPolicy() == data.getTimeoutPolicy()
        saga.getConcurrentLimitNum() == saga.getConcurrentLimitNum()
    }

    def 'stringArrayJoin'() {
        given: '创建一个list给出join字符串'
        def list = ['abc', 'def', 'ghi', 'uk']
        def join = '&%'

        when: '执行stringArrayJoin'
        def data = ConvertUtils.stringArrayJoin(list, join)

        then: '验证结果'
        data == 'abc&%def&%ghi&%uk'
    }

    def 'convertToJsonMerge'() {
        given: '创建一个SagaTaskInstance的集合，并数据库插入json数据'
        def jsonData1 = new JsonData(JsonOutput.toJson([name: 'John2', id: 2, pass: 'valJest']))
        def jsonData2 = new JsonData(JsonOutput.toJson([id: 23, value: 'valJest']))
        jsonDataMapper.insert(jsonData1)
        jsonDataMapper.insert(jsonData2)
        def list = [new SagaTaskInstance('code1', jsonData1.getId()), new SagaTaskInstance('code2', jsonData2.getId())]

        when: '调用convertToJsonMerge方法'
        def jsonMergeDTOS = ConvertUtils.convertToJsonMerge(list, jsonDataMapper)

        then: '验证结果'
        jsonMergeDTOS.size() == 2
        jsonMergeDTOS.get(0).getTaskCode() == list.get(0).getTaskCode()
        jsonMergeDTOS.get(0).getTaskOutputJsonData() == jsonData1.getData()
        jsonMergeDTOS.get(1).getTaskCode() == list.get(1).getTaskCode()
        jsonMergeDTOS.get(1).getTaskOutputJsonData() == jsonData2.getData()
    }

    def 'jsonMerge'() {
        given: '创建几个JsonMergeDTO'
        def objectMapper = new ObjectMapper()
        def jsonSlurper = new JsonSlurper()
        def data1 = new JsonMergeDTO('code1', JsonOutput.toJson([name: 'John1', pass: 'valJest']))
        def data2 = new JsonMergeDTO('code2', JsonOutput.toJson([name: 'John2', id: 2]))
        def data3 = new JsonMergeDTO('code4', 'false')

        when: '执行jsonMerge'
        def map1 = jsonSlurper.parseText(ConvertUtils.jsonMerge([data1, data2], objectMapper))
        def map2 = jsonSlurper.parseText(ConvertUtils.jsonMerge([data1, data3], objectMapper))
        map1 = (Map)map1
        map2 = (Map)map2

        then: "验证jsonMerge结果"
        map1.get('name') == 'John2'
        map1.get('pass') == 'valJest'
        map1.get('id') == 2
        map2.get('name') == 'John1'
        map2.get('pass') == 'valJest'
        map2.get('code4') == false

    }
}
