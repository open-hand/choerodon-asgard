package io.choerodon.asgard.infra.utils

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.choerodon.asgard.IntegrationTestConfiguration
import io.choerodon.asgard.api.dto.JsonMergeDTO
import io.choerodon.asgard.domain.JsonData
import io.choerodon.asgard.domain.SagaTaskInstance
import io.choerodon.asgard.infra.mapper.JsonDataMapper
import io.choerodon.asgard.saga.SagaDefinition
import io.choerodon.asgard.saga.property.PropertyData
import org.modelmapper.ModelMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import spock.lang.Shared
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class ConvertUtilsSpec extends Specification {

    @Autowired
    JsonDataMapper jsonDataMapper

    @Shared
    def mapper = new ModelMapper()

    def '测试 convertSaga方法'() {
        given: '创建一个PropertyData.Saga'
        def test = new PropertyData.Saga('code', 'desc')
        test.setInputSchema('name,id')
        test.setInputSchemaSource('data')
        def service = 'convertSaga'

        when: '调用ConvertUtils的convertSaga方法'
        def result = ConvertUtils.convertSaga(mapper, test, service)

        then: '验证转换结果'
        result.getService() == service
        result.getInputSchema() == test.getInputSchema()
        result.getInputSchemaSource() == test.getInputSchemaSource()
        result.getCode() == test.getCode()
        result.getDescription() == test.getDescription()
    }

    def '测试 convertSagaTask方法'() {
        given: '创建一个PropertyData.SagaTask'
        def data = new PropertyData.SagaTask('code', 'desc', 'sagaCode', 20, 33)
        data.setTimeoutSeconds(10)
        data.setTimeoutPolicy(SagaDefinition.TimeoutPolicy.ALERT_ONLY.name())
        data.setConcurrentLimitNum(10)
        def service = 'convertSaga'

        when: '调用ConvertUtils的convertSaga方法'
        def saga = ConvertUtils.convertSagaTask(mapper, data, service)

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


    def '测试 convertToJsonMerge方法'() {
        given: '创建一个SagaTaskInstance的集合，并数据库插入json数据'
        def jsonData1 = new JsonData(JsonOutput.toJson([name: 'John2', id: 2, pass: 'valJest']))
        def jsonData2 = new JsonData(JsonOutput.toJson([id: 23, value: 'valJest']))
        jsonDataMapper.insert(jsonData1)
        jsonDataMapper.insert(jsonData2)
        def list = [new SagaTaskInstance('code1', jsonData1.getId()), new SagaTaskInstance('code2', jsonData2.getId())]
        def emptyDataIdslist = [new SagaTaskInstance('code1', null), new SagaTaskInstance('code2', jsonData2.getId())]

        when: '调用convertToJsonMerge方法'
        def jsonMergeDTOS = ConvertUtils.convertToJsonMerge(list, jsonDataMapper)
        def emptyDataIdJsonMergeDTOS = ConvertUtils.convertToJsonMerge(emptyDataIdslist, jsonDataMapper)

        then: '验证结果'
        jsonMergeDTOS.size() == 2
        jsonMergeDTOS.get(0).getTaskCode() == list.get(0).getTaskCode()
        jsonMergeDTOS.get(0).getTaskOutputJsonData() == jsonData1.getData()
        jsonMergeDTOS.get(1).getTaskCode() == list.get(1).getTaskCode()
        jsonMergeDTOS.get(1).getTaskOutputJsonData() == jsonData2.getData()
        emptyDataIdJsonMergeDTOS.size() == 1
    }

    def '测试 jsonMerge方法'() {
        given: '创建几个JsonMergeDTO'
        def objectMapper = new ObjectMapper()
        def jsonSlurper = new JsonSlurper()
        def data1 = new JsonMergeDTO('code1', JsonOutput.toJson([name: 'John1', pass: 'valJest']))
        def data2 = new JsonMergeDTO('code2', JsonOutput.toJson([name: 'John2', id: 2]))
        def data3 = new JsonMergeDTO('code3', 'false')
        def data4 = new JsonMergeDTO('code4', objectMapper.writeValueAsString(['one','two'] as String[]))

        when: '执行jsonMerge'
        def map1 = jsonSlurper.parseText(ConvertUtils.jsonMerge([data1, data2], objectMapper))
        def map2 = jsonSlurper.parseText(ConvertUtils.jsonMerge([data1, data3], objectMapper))
        def map3 = jsonSlurper.parseText(ConvertUtils.jsonMerge([data3, data4], objectMapper))
        def map4 = jsonSlurper.parseText(ConvertUtils.jsonMerge([data3], objectMapper))
        def emptyList = ConvertUtils.jsonMerge([], objectMapper)
        map1 = (Map) map1
        map2 = (Map) map2
        map3 = (Map) map3

        then: "验证jsonMerge结果"
        emptyList == '{}'
        map1.get('name') == 'John2'
        map1.get('pass') == 'valJest'
        map1.get('id') == 2

        map2.get('name') == 'John1'
        map2.get('pass') == 'valJest'
        map2.get('code3') == false

        map3.get('code3') == false
        ((List)map3.get('code4')).get(0) == 'one'
        ((List)map3.get('code4')).get(1) == 'two'

        map4 == false
    }
}
