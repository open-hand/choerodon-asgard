package io.choerodon.asgard.api.service

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import io.choerodon.asgard.IntegrationTestConfiguration
import io.choerodon.asgard.api.dto.SagaWithTaskInstanceDTO
import io.choerodon.asgard.api.dto.StartInstanceDTO
import io.choerodon.asgard.api.service.impl.SagaInstanceServiceImpl
import io.choerodon.asgard.domain.*
import io.choerodon.asgard.infra.mapper.*
import io.choerodon.asgard.saga.SagaDefinition
import io.choerodon.core.exception.CommonException
import io.choerodon.mybatis.pagehelper.domain.PageRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.Transactional
import spock.lang.Specification
import spock.lang.Stepwise

import java.text.SimpleDateFormat

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Stepwise
@Transactional
class SagaInstanceServiceSpec extends Specification {

    /**
     * 调用的mapper可以采用mock的方式
     * 也可以调用真实mapper,方法添加了@Transactional,防止数据库数据对其他测试方法有影响
     */
    @Autowired
    SagaInstanceService sagaInstanceService

    @Autowired
    SagaMapper sagaMapper

    @Autowired
    SagaTaskMapper sagaTaskMapper

    @Autowired
    SagaInstanceMapper sagaInstanceMapper

    @Autowired
    SagaTaskInstanceMapper sagaTaskInstanceMapper

    @Autowired
    JsonDataMapper jsonDataMapper

    def mapper = new ObjectMapper()


    def '测试 query方法'() {
        given: '数据库插入测试数据'
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"))
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true)
        def sagaInstance = new SagaInstance('query', 'type', '', 'id', new Date())
        sagaInstanceMapper.insertSelective(sagaInstance)
        def json = new JsonData('data')
        jsonDataMapper.insert(json)
        def taskInstanceOne = createSagaTaskInstance('one', sagaInstance.getId(), 2, json.getId())
        def taskInstanceTwo = createSagaTaskInstance('two', sagaInstance.getId(), 8, json.getId())
        def taskInstanceThree = createSagaTaskInstance('three', sagaInstance.getId(), 8, json.getId())
        sagaTaskInstanceMapper.insertSelective(taskInstanceOne)
        sagaTaskInstanceMapper.insertSelective(taskInstanceTwo)
        sagaTaskInstanceMapper.insertSelective(taskInstanceThree)

        when: '查询不存在的id'
        sagaInstanceService.query(9999L)

        then: '抛出CommonException'
        CommonException notExist = thrown CommonException
        notExist.getCode() == 'error.sagaInstance.notExist'

        when: '查询存在的id'
        String result = sagaInstanceService.query(sagaInstance.getId()).getBody()

        then: '验证查询数据'
        def dto = mapper.readValue(result, SagaWithTaskInstanceDTO)
        dto.getSagaCode() == sagaInstance.getSagaCode()
        dto.getTasks().size() == 2
        dto.getTasks().get(0).size() == 1
        dto.getTasks().get(0).get(0).getTaskCode() == taskInstanceOne.getTaskCode()
        dto.getTasks().get(1).size() == 2
        dto.getTasks().get(1).get(0).getSeq() == taskInstanceTwo.getSeq()
    }

    def '测试 start方法'() {
        given: '数据库插入测试所需要数据'
        def existTaskSaga = new Saga('code', 'iam', 'desc', 'schema', 'schema')
        def notExistTaskSaga = new Saga('notExistTask', 'iam', 'desc', 'schema', 'schema')
        def firstTask = new SagaTask('one', existTaskSaga.getCode(), 2, true, 'iam')
        def secondTask = new SagaTask('two', existTaskSaga.getCode(), 2, true, 'iam')
        def thirdTask = new SagaTask('three', existTaskSaga.getCode(), 5, true, 'iam')
        def fourthTask = new SagaTask('four', existTaskSaga.getCode(), 5, false, 'iam')
        sagaMapper.insertSelective(existTaskSaga)
        sagaMapper.insertSelective(notExistTaskSaga)
        sagaTaskMapper.insertSelective(firstTask)
        sagaTaskMapper.insertSelective(secondTask)
        sagaTaskMapper.insertSelective(thirdTask)
        sagaTaskMapper.insertSelective(fourthTask)

        and: '创建测试DTO对象'
        def dto = new StartInstanceDTO()
        dto.setInput('{}')
        dto.setRefId('id')
        dto.setRefType('type')

        when: '用不存在task的code调用start方法'
        dto.setSagaCode(notExistTaskSaga.getCode())
        def notExistTaskResponse = sagaInstanceService.start(dto)

        then: '数据库插入一条SagaInstance'
        def notExistInstance = sagaInstanceMapper.selectOne(new SagaInstance(notExistTaskSaga.getCode()))
        notExistInstance != null
        notExistInstance.getSagaCode() == notExistTaskSaga.getCode()
        notExistTaskResponse.getBody().getSagaCode() == notExistTaskSaga.getCode()

        when: '用存在task的code调用start方法'
        dto.setSagaCode(existTaskSaga.getCode())
        def existTaskResponse = sagaInstanceService.start(dto)

        then: '数据库插入一条SagaInstance、三条SagaTaskInstance'
        def existInstance = sagaInstanceMapper.selectOne(new SagaInstance(existTaskSaga.getCode()))
        existInstance != null
        existTaskResponse.getBody().getSagaCode() == existTaskSaga.getCode()
        def query = new SagaTaskInstance()
        existInstance.getStatus() == SagaDefinition.InstanceStatus.RUNNING.name()
        query.setSagaInstanceId(existInstance.getId())
        def existTaskInstances = sagaTaskInstanceMapper.select(query)
        existTaskInstances.size() == 3
        def firstInstance = getInstanceFromListByCode(existTaskInstances, firstTask.getCode())
        firstInstance.getStatus() == SagaDefinition.TaskInstanceStatus.RUNNING.name()
        firstInstance.getInputDataId() != null
        def secondInstance = getInstanceFromListByCode(existTaskInstances, secondTask.getCode())
        secondInstance.getStatus() == SagaDefinition.TaskInstanceStatus.RUNNING.name()
        secondInstance.getInputDataId() != null
        def thirdInstance = getInstanceFromListByCode(existTaskInstances, thirdTask.getCode())
        thirdInstance.getStatus() == SagaDefinition.TaskInstanceStatus.QUEUE.name()
        thirdInstance.getInputDataId() == null
    }

    SagaTaskInstance getInstanceFromListByCode(List<SagaTaskInstance> list, String code) {
        for (SagaTaskInstance instance : list) {
            if (instance.getTaskCode() == code) {
                return instance
            }
        }
        return null
    }

    def '测试 pageQuery方法'() {
        given: 'mock instanceMapper'
        def instanceMapper = Mock(SagaInstanceMapper)

        and: 'mock对象注入SagaInstanceService'
        def tempSagaInstanceService = new SagaInstanceServiceImpl(null, null, instanceMapper, null, null)

        when: '调用pageQuery方法'
        tempSagaInstanceService.pageQuery(new PageRequest(), "", "", "", "", "")

        then: '验证SagaInstanceMapper的fulltextSearch方法被调用'
        1 * instanceMapper.fulltextSearch(_, _, _, _, _)
    }


    def createSagaTaskInstance(String taskCode, long instanceId, int seq, long dataId) {
        return SagaTaskInstanceBuilder.aSagaTaskInstance()
                .withTaskCode(taskCode)
                .withSagaCode('query')
                .withSeq(seq)
                .withSagaInstanceId(instanceId)
                .withMaxRetryCount(1)
                .withTimeoutPolicy(SagaDefinition.TimeoutPolicy.RETRY.name())
                .withInputDataId(dataId)
                .withOutputDataId(dataId)
                .withTimeoutSeconds(1).withRetriedCount(0)
                .withMaxRetryCount(1)
                .withConcurrentLimitPolicy(SagaDefinition.ConcurrentLimitPolicy.NONE.name())
                .withConcurrentLimitNum(1)
                .withStatus(SagaDefinition.TaskInstanceStatus.RUNNING.name()).build()
    }

}
