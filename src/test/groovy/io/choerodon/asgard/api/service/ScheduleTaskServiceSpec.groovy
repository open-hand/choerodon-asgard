package io.choerodon.asgard.api.service

import io.choerodon.asgard.IntegrationTestConfiguration
import io.choerodon.asgard.api.dto.ScheduleTaskDTO
import io.choerodon.asgard.api.service.impl.ScheduleTaskServiceImpl
import io.choerodon.asgard.domain.QuartzMethod
import io.choerodon.asgard.domain.QuartzTask
import io.choerodon.asgard.domain.QuartzTaskDetail
import io.choerodon.asgard.domain.QuartzTaskInstance
import io.choerodon.asgard.infra.mapper.QuartzMethodMapper
import io.choerodon.asgard.infra.mapper.QuartzTaskInstanceMapper
import io.choerodon.asgard.infra.mapper.QuartzTaskMapper
import io.choerodon.asgard.property.PropertyTimedTask
import io.choerodon.asgard.schedule.QuartzDefinition
import io.choerodon.core.domain.Page
import io.choerodon.core.exception.CommonException
import io.choerodon.mybatis.pagehelper.domain.PageRequest
import io.choerodon.mybatis.pagehelper.domain.Sort
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class ScheduleTaskServiceSpec extends Specification {
    private ScheduleTaskServiceImpl scheduleTaskService

    private QuartzMethodMapper mockMethodMapper = Mock(QuartzMethodMapper)

    private QuartzTaskMapper mockTaskMapper = Mock(QuartzTaskMapper)

    private QuartzJobService mockQuartzJobService = Mock(QuartzJobService)

    private QuartzTaskInstanceMapper mockInstanceMapper = Mock(QuartzTaskInstanceMapper)

    void setup() {
        scheduleTaskService = new ScheduleTaskServiceImpl(mockMethodMapper, mockTaskMapper, mockQuartzJobService, mockInstanceMapper)
    }

    def "Create[Exception]"() {
        given: '准备方法参数dto'
        def map = new HashMap<String, Object>()
        map.put("name", null)
        map.put("age", 11)
        def dto = new ScheduleTaskDTO()
        dto.setParams(map)
        and: 'mock'
        mockMethodMapper.selectByPrimaryKey(_) >> { return method }
        mockTaskMapper.insertSelective(_) >> { return insert }
        when: '方法调用'
        scheduleTaskService.create(dto)
        then: '结果分析'
        def e = thrown(exceptionType)
        e.message == msg
        where: "异常比对"
        method                                                                                                                                                                                                                       | insert || exceptionType   | msg
        null                                                                                                                                                                                                                         | 0      || CommonException | "error.scheduleTask.methodNotExist"
        new QuartzMethod(code: "code", params: "[{\"name\":\"name\",\"defaultValue\":null,\"type\":\"String\",\"description\":\"\"},{\"name\":\"age\",\"defaultValue\":null,\"type\":\"Integer\",\"description\":\"年龄\"}]")          | 0      || CommonException | "error.scheduleTask.paramInvalidType"
        new QuartzMethod(code: "code", params: "[{\"name\":\"name\",\"defaultValue\":\"zh\",\"type\":\"String\",\"description\":\"\"},{\"name\":\"age\",\"defaultValue\":null,\"type\":\"IntegerInValid\",\"description\":\"年龄\"}]") | 0      || CommonException | "error.scheduleTask.paramType"
        new QuartzMethod(code: "code", params: "[{\"name\":\"name\",\"defaultValue\":\"zh\",\"type\":\"String\",\"description\":\"\"},{\"name\":\"age\",\"defaultValue\":null,\"type\":\"Integer\",\"description\":\"年龄\"}]")        | 0      || CommonException | "error.scheduleTask.create"
    }

    def "Create[IOException]"() {
        given: '准备方法参数dto'
        def map = new HashMap<String, Object>()
        map.put("name", "Amy")
        map.put("age", 11)
        def dto = new ScheduleTaskDTO()
        dto.setParams(map)
        and: 'mock'
        mockMethodMapper.selectByPrimaryKey(_) >> {
            return new QuartzMethod(code: "code", params: "[{\"name\",\"name\",\"defaultValue\":\"zh\",\"type\":\"String\",\"description\":\"\"},{\"name\":\"age\",\"defaultValue\":null,\"type\":\"Integer\",\"description\":\"年龄\"}]")
        }
        when: '方法调用'
        scheduleTaskService.create(dto)
        then: '结果分析'
        def e = thrown(CommonException)
        e.message == "error.scheduleTask.createJsonIOException"
    }

    def "Create"() {
        given: '准备方法参数dto'
        def map = new HashMap<String, Object>()
        map.put("name", "Amy")
        map.put("age", 11)
        map.put("long", new Long(1111))
        map.put("bool", false)
        map.put("double", new Double(1.1))
        def dto = new ScheduleTaskDTO()
        dto.setParams(map)
        def params = "[" +
                "{\"name\":\"name\",\"defaultValue\":\"zh\",\"type\":\"String\",\"description\":\"字符串\"}," +
                "{\"name\":\"age\",\"defaultValue\":null,\"type\":\"Integer\",\"description\":\"整型\"}," +
                "{\"name\":\"bool\",\"defaultValue\":true,\"type\":\"Boolean\",\"description\":\"布尔类型\"}," +
                "{\"name\":\"long\",\"defaultValue\":1,\"type\":\"Long\",\"description\":\"Long类型\"}," +
                "{\"name\":\"double\",\"defaultValue\":1.1,\"type\":\"Double\",\"description\":\"双精度浮点\"}" +
                "]"

        and: 'mock'
        mockMethodMapper.selectByPrimaryKey(_) >> {
            return new QuartzMethod(code: "code", params: params)
        }
        mockTaskMapper.insertSelective(_) >> { return 1 }
        when: '方法调用'
        scheduleTaskService.create(dto)
        then: '结果分析'
        noExceptionThrown()
    }

    def "Enable"() {
        given: '参数准备'
        def id = 1L
        def objectVersionNumber = 1L

        and: 'mock'
        mockTaskMapper.selectByPrimaryKey(_) >> { return new QuartzTask(id: 1L, status: 'DISABLE') }
        mockTaskMapper.updateByPrimaryKey(_) >> { return 1 }

        when: '方法调用'
        scheduleTaskService.enable(id, objectVersionNumber)
        then: '结果分析'
        noExceptionThrown()
    }

    def "Enable[Exception]"() {
        given: '参数准备'
        def id = 1L
        def objectVersionNumber = 1L

        and: 'mock'
        mockTaskMapper.selectByPrimaryKey(_) >> { return dto }
        mockTaskMapper.updateByPrimaryKey(_) >> { return 0 }

        when: '方法调用'
        scheduleTaskService.enable(id, objectVersionNumber)
        then: '结果分析'
        def error = thrown(CommonException)
        error.message == errormsg
        where: '异常分析'
        dto                                       || errormsg
        null                                      || "error.scheduleTask.taskNotExist"
        new QuartzTask(id: 1L, status: 'DISABLE') || "error.scheduleTask.enableTaskFailed"

    }

    def "Disable"() {
        given: '参数准备'
        def id = 1L
        def objectVersionNumber = 1L

        and: 'mock'
        mockTaskMapper.selectByPrimaryKey(_) >> { return new QuartzTask(id: 1L, status: 'ENABLE') }
        mockTaskMapper.updateByPrimaryKey(_) >> { return 1 }

        when: '方法调用'
        scheduleTaskService.disable(id, objectVersionNumber, true)
        then: '结果分析'
        noExceptionThrown()
    }

    def "Disable[Exception]"() {
        given: '参数准备'
        def id = 1L
        def objectVersionNumber = 1L
        def executeWithIn = true

        and: 'mock'
        mockTaskMapper.selectByPrimaryKey(_) >> { return dto }
        mockTaskMapper.updateByPrimaryKey(_) >> { return 0 }

        when: '方法调用'
        scheduleTaskService.disable(id, objectVersionNumber, executeWithIn)
        then: '结果分析'
        def error = thrown(CommonException)
        error.message == errormsg
        where: '异常分析'
        dto                                                               || errormsg
        null                                                              || "error.scheduleTask.taskNotExist"
        new QuartzTask(id: 1L, status: 'ENABLE', objectVersionNumber: 1L) || "error.scheduleTask.disableTaskFailed"
    }

    def "Delete"() {
        given: '参数准备'
        def id = 1L

        and: 'mock'
        mockTaskMapper.selectByPrimaryKey(_) >> {
            return new QuartzTask(id: 1L, status: 'ENABLE', objectVersionNumber: 1L)
        }
        mockTaskMapper.deleteByPrimaryKey(_) >> { return 1 }

        when: '方法调用'
        scheduleTaskService.delete(id)
        then: '结果分析'
        noExceptionThrown()
    }

    def "Delete[Exception]"() {
        given: '参数准备'
        def id = 1L

        and: 'mock'
        mockTaskMapper.selectByPrimaryKey(_) >> { return dto }
        mockTaskMapper.deleteByPrimaryKey(_) >> { return 0 }

        when: '方法调用'
        scheduleTaskService.delete(id)
        then: '结果分析'
        def error = thrown(CommonException)
        error.message == errormsg
        where: '异常分析'
        dto                                                               || errormsg
        null                                                              || "error.scheduleTask.taskNotExist"
        new QuartzTask(id: 1L, status: 'ENABLE', objectVersionNumber: 1L) || "error.scheduleTask.deleteTaskFailed"
    }

    def "PageQuery"() {
        given: '准备参数'
        def status = "RUNNING"
        def name = ""
        def description = ''
        def params = ""

        def taskList = new ArrayList<QuartzTask>()
        def quartzTask = new QuartzTask()
        quartzTask.setId(1L)
        quartzTask.setDescription("description")
        quartzTask.setStatus("RUNNING")
        quartzTask.setName("name")
        taskList.add(quartzTask)
        and: "构造pageRequest"
        def order = new Sort.Order("id")
        def pageRequest = new PageRequest(1, 20, new Sort(order))
        and: "mock"
        mockTaskMapper.fulltextSearch(_, _, _, _) >> { return taskList }
        when: '调用方法'
        scheduleTaskService.pageQuery(pageRequest, status, name, description, params)
        then: '无异常抛出'
        thrown(NullPointerException)
    }

    def "pageConvert"() {
        given: "参数准备"
        def name = "name"
        def objectVersionNumber = 1L
        def status = "ENABLE"
        def description = "description"
        def startTime = new Date()
        QuartzTask quartzTask = new QuartzTask()
        quartzTask.setId(1L)
        quartzTask.setName(name)
        quartzTask.setDescription(description)
        quartzTask.setObjectVersionNumber(objectVersionNumber)
        quartzTask.setStatus(status)
        quartzTask.setStartTime(startTime)

        List<QuartzTask> list = new ArrayList<>()
        list.add(quartzTask)

        def page = new Page<QuartzTask>()
        page.setNumber(1)
        page.setNumberOfElements(1)
        page.setTotalPages(1)
        page.setTotalElements(1L)

        when: "方法调用"
        scheduleTaskService.pageConvert(page)

        then: "结果分析"
        noExceptionThrown()

        and: "content添加"
        page.setContent(list)
        and: "mock"
        mockInstanceMapper.selectLastInstance(_) >> { return null }

        when: "方法调用"
        def convert2 = scheduleTaskService.pageConvert(page)

        then: "结果分析"
        noExceptionThrown()
        convert2.getContent().size() == list.size()
    }

    def "pageConvert[lastInstance!=null]"() {
        given: "参数准备"
        def name = "name"
        def objectVersionNumber = 1L
        def status = "ENABLE"
        def description = "description"
        def startTime = new Date()
        QuartzTask quartzTask = new QuartzTask()
        quartzTask.setId(1L)
        quartzTask.setName(name)
        quartzTask.setDescription(description)
        quartzTask.setObjectVersionNumber(objectVersionNumber)
        quartzTask.setStatus(status)
        quartzTask.setStartTime(startTime)

        List<QuartzTask> list = new ArrayList<>()
        list.add(quartzTask)

        def page = new Page<QuartzTask>()
        page.setNumber(1)
        page.setNumberOfElements(1)
        page.setTotalPages(1)
        page.setTotalElements(1L)

        and: "content添加"
        page.setContent(list)

        and: "mock"
        mockInstanceMapper.selectLastInstance(_) >> {
            return new QuartzTaskInstance(actualStartTime: new Date(), plannedNextTime: new Date())
        }

        when: "方法调用"
        def convert = scheduleTaskService.pageConvert(page)

        then: "结果分析"
        noExceptionThrown()
        convert.getContent().size() == list.size()
    }

    def "Finish"() {
        given: "参数准备"
        def id = 1L
        and: "mock"
        mockTaskMapper.selectByPrimaryKey(_) >> { return dto }
        mockTaskMapper.updateByPrimaryKey(_) >> { return num }
        when: "方法调用"
        scheduleTaskService.finish(id)
        then: "结果分析"
        noExceptionThrown()
        where: "条件分支"
        dto              | num
        null             | 0
        new QuartzTask() | 1
        new QuartzTask() | 0
    }

    def "GetTaskDetail[Exception]"() {
        given: "参数准备"
        def id = 1L
        and: "mock"
        mockTaskMapper.selectByPrimaryKey(_) >> { return null }
        when: '方法调用'
        scheduleTaskService.getTaskDetail(id)
        then: "结果分析"
        def e = thrown(CommonException)
        e.message == "error.scheduleTask.taskNotExist"
    }

    def "GetTaskDetail[lastInstance!=null]"() {
        given: "参数准备"
        def id = 1L

        def detail = new QuartzTaskDetail()
        detail.setId(1L)
        detail.setParams("{\"isInstantly\",true,\"name\",\"zh\",\"age\",20}")
        and: "mock"
        mockTaskMapper.selectByPrimaryKey(_) >> { return new QuartzTask(startTime: new Date()) }
        mockInstanceMapper.selectLastInstance(_) >> { return null }
        mockTaskMapper.selectTaskById(_) >> { return detail }
        when: '方法调用'
        scheduleTaskService.getTaskDetail(id)
        then: "结果分析"
        def e = thrown(CommonException)
        e.message == "error.scheduleTaskDetailDTO.jsonIOException"
    }

    def "GetTaskDetail"() {
        given: "参数准备"
        def id = 1L

        def detail = new QuartzTaskDetail()
        detail.setId(1L)
        detail.setParams("{\"isInstantly\":true,\"name\":\"zh\",\"age\":20}")
        and: "mock"
        mockTaskMapper.selectByPrimaryKey(_) >> { return new QuartzTask(startTime: new Date()) }
        mockInstanceMapper.selectLastInstance(_) >> {
            return new QuartzTaskInstance(actualStartTime: new Date(), plannedNextTime: new Date())
        }
        mockTaskMapper.selectTaskById(_) >> { return detail }
        when: '方法调用'
        scheduleTaskService.getTaskDetail(id)
        then: "结果分析"
        noExceptionThrown()
    }

    def "CheckName"() {
        given: "参数准备"
        def name = "name"
        def list = new ArrayList<Long>()
        list.add(1L)

        and: "mock"
        mockTaskMapper.selectTaskIdByName(name) >> { return new ArrayList<Long>() }

        when: "方法调用"
        scheduleTaskService.checkName(name)
        then: "抛出异常"
        noExceptionThrown()
    }

    def "CheckName[Exception]"() {
        given: "参数准备"
        def name = "name"
        def list = new ArrayList<Long>()
        list.add(1L)

        and: "mock"
        mockTaskMapper.selectTaskIdByName(name) >> { return list }

        when: "方法调用"
        scheduleTaskService.checkName(name)
        then: "抛出异常"
        def e = thrown(CommonException)
        e.message == "error.scheduleTask.name.exist"
    }

    def "CreateTaskList1-[methodNotExist]"() {
        given: "参数准备"
        def service = "service"
        def scanTasks = new ArrayList<PropertyTimedTask>()

        def task = new PropertyTimedTask()
        task.setName("name")
        task.setDescription("description")
        task.setMethodCode("methodCode")
        task.setOneExecution(true)
        task.setRepeatCount(0)
        task.setRepeatInterval(100)
        task.setRepeatIntervalUnit(QuartzDefinition.SimpleRepeatIntervalUnit.SECONDS.name())
        Map<String, Object> map = new HashMap<>()
        map.put("name1", "value1")
        task.setParams(map)

        scanTasks.add(task)
        def version = "version"
        and: "mock"
        mockMethodMapper.select(_) >> { return new ArrayList<QuartzMethod>() }
        when: "方法啊调用"
        scheduleTaskService.createTaskList(service, scanTasks, version)
        then: "抛出异常"
        def e = thrown(CommonException)
        e.message == "error.scheduleTask.methodNotExist"
    }

    def "CreateTaskList2"() {
        given: "参数准备"
        def service = "service"
        def scanTasks = new ArrayList<PropertyTimedTask>()

        def task = new PropertyTimedTask()
        task.setName("name")
        task.setDescription("description")
        task.setMethodCode("methodCode")
        task.setOneExecution(false)
        task.setRepeatCount(0)
        task.setRepeatInterval(100)
        task.setRepeatIntervalUnit(QuartzDefinition.SimpleRepeatIntervalUnit.SECONDS.name())
        Map<String, Object> map = new HashMap<>()
        map.put("name1", "value1")
        task.setParams(map)

        scanTasks.add(task)

        def version = "version2"
        and: "mock"
        mockMethodMapper.select(_) >> {
            def methods = new ArrayList<QuartzMethod>()
            def method = new QuartzMethod()
            method.setCode("methodCode")
            method.setParams("[{\"name\":\"name1\",\"defaultValue\":\"dv\",\"type\":\"String\",\"description\":\"description\"}]")
            methods.add(method)
            return methods
        }

        mockTaskMapper.select(_) >> {
            def dbTasks = new ArrayList<QuartzTask>()
            def task1 = new QuartzTask()
            task1.setName("name:version1")
            return dbTasks
        }
        mockTaskMapper.insertSelective(_) >> { return 1 }
        when: "方法啊调用"
        scheduleTaskService.createTaskList(service, scanTasks, version)
        then: "抛出异常"
        noExceptionThrown()
    }
}
