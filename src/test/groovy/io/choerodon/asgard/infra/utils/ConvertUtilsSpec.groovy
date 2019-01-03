package io.choerodon.asgard.infra.utils

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.choerodon.asgard.IntegrationTestConfiguration
import io.choerodon.asgard.api.dto.JsonMergeDTO
import io.choerodon.asgard.domain.JsonData
import io.choerodon.asgard.domain.SagaTaskInstance
import io.choerodon.asgard.infra.mapper.JsonDataMapper
import io.choerodon.asgard.property.PropertyJobParam
import io.choerodon.asgard.property.PropertyJobTask
import io.choerodon.asgard.property.PropertySaga
import io.choerodon.asgard.property.PropertySagaTask
import io.choerodon.asgard.saga.SagaDefinition
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

    def '测试 convertQuartzMethod方法'() {
        given: "参数准备"
        def service = "service"
        def jobTask = new PropertyJobTask()
        jobTask.setCode("code")
        jobTask.setDescription("description")
        jobTask.setMethod("method")
        jobTask.setMaxRetryCount(2)
        def jobParam = new PropertyJobParam()
        jobParam.setDefaultValue("dv")
        jobParam.setDescription("description")
        jobParam.setName("name")
        jobParam.setType("String")
        jobTask.setLevel("site")
        def list = new ArrayList<PropertyJobParam>()
        list.add(jobParam)
        jobTask.setParams(list)

        when: "调用ConvertUtils的convertQuartzMethod方法"
        def method = ConvertUtils.convertQuartzMethod(new ObjectMapper(), jobTask, service)

        then: "结果换算验证"
        method.getService() == service
        method.getMethod() == jobTask.getMethod()
        method.getCode() == jobTask.getCode()
        method.getParams() == "[{\"name\":\"name\",\"defaultValue\":\"dv\",\"type\":\"String\",\"description\":\"description\"}]"
        method.getDescription() == jobTask.getDescription()
        method.getMaxRetryCount() == jobTask.getMaxRetryCount()
    }

    def '测试 convertSaga方法'() {
        given: '创建一个PropertyData.Saga'
        def test = new PropertySaga('code', 'desc')
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
        def data4 = new JsonMergeDTO('code4', objectMapper.writeValueAsString(['one', 'two'] as String[]))

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
        ((List) map3.get('code4')).get(0) == 'one'
        ((List) map3.get('code4')).get(1) == 'two'

        map4 == false
    }
}
