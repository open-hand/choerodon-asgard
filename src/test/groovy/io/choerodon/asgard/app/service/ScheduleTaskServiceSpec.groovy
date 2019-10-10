package io.choerodon.asgard.app.service

import io.choerodon.asgard.IntegrationTestConfiguration
import io.choerodon.asgard.api.vo.QuartzTask
import io.choerodon.asgard.api.vo.QuartzTaskDetail
import io.choerodon.asgard.api.vo.Role
import io.choerodon.asgard.api.vo.ScheduleTask

import io.choerodon.asgard.infra.dto.QuartzMethodDTO
import io.choerodon.asgard.infra.dto.QuartzTaskDTO
import io.choerodon.asgard.infra.dto.QuartzTaskInstanceDTO
import io.choerodon.asgard.infra.feign.IamFeignClient
import io.choerodon.asgard.infra.feign.NotifyFeignClient
import io.choerodon.asgard.infra.mapper.QuartzMethodMapper
import io.choerodon.asgard.infra.mapper.QuartzTaskInstanceMapper
import io.choerodon.asgard.infra.mapper.QuartzTaskMapper
import io.choerodon.asgard.infra.mapper.QuartzTaskMemberMapper
import io.choerodon.asgard.property.PropertyTimedTask
import io.choerodon.asgard.schedule.QuartzDefinition
import io.choerodon.base.domain.PageRequest
import io.choerodon.core.exception.CommonException
import io.choerodon.core.iam.ResourceLevel
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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

    private IamFeignClient iamFeignClient = Mock(IamFeignClient)

    private NotifyFeignClient notifyFeignClient = Mock(NotifyFeignClient)

    private NoticeService noticeService = new NoticeServiceImpl(notifyFeignClient, iamFeignClient)

    private QuartzTaskMemberMapper mockQuartzTaskMemberMapper = Mock(QuartzTaskMemberMapper)

    void setup() {
        scheduleTaskService = new ScheduleTaskServiceImpl(mockMethodMapper,
                mockTaskMapper, mockQuartzJobService, mockInstanceMapper,
                mockQuartzTaskMemberMapper, iamFeignClient, noticeService)
    }

    def "Create[Exception]"() {
        given: '准备方法参数dto'
        def map = new HashMap<String, Object>()
        map.put("name", null)
        map.put("age", 11)
        def dto = new ScheduleTask()
        dto.setParams(map)
        and: 'mock'
        mockMethodMapper.selectByPrimaryKey(_) >> { return method }
        mockTaskMapper.insertSelective(_) >> { return insert }
        when: '方法调用'
        scheduleTaskService.create(dto, "site", 0L)
        then: '结果分析'
        def e = thrown(exceptionType)
        e.message == msg
        where: "异常比对"
        method                                                                                                                                                                                                                                      | insert || exceptionType   | msg
        null                                                                                                                                                                                                                                        | 0      || CommonException | "error.scheduleTask.methodNotExist"
        new QuartzMethodDTO(code: "code", level: "site", params: "[{\"name\":\"name\",\"defaultValue\":null,\"type\":\"String\",\"description\":\"\"},{\"name\":\"age\",\"defaultValue\":null,\"type\":\"Integer\",\"description\":\"年龄\"}]")       | 0      || CommonException | "error.scheduleTask.paramInvalidType"
        new QuartzMethodDTO(code: "code", level: "site", params: "[{\"name\":\"name\",\"defaultValue\":\"zh\",\"type\":\"String\",\"description\":\"\"},{\"name\":\"age\",\"defaultValue\":null,\"type\":\"IntegerInValid\",\"description\":\"年龄\"}]") | 0      || CommonException | "error.scheduleTask.paramType"
        new QuartzMethodDTO(code: "code", level: "site", params: "[{\"name\":\"name\",\"defaultValue\":\"zh\",\"type\":\"String\",\"description\":\"\"},{\"name\":\"age\",\"defaultValue\":null,\"type\":\"Integer\",\"description\":\"年龄\"}]")        | 0      || CommonException | "error.scheduleTask.create"
    }

    def "Create[IOException]"() {
        given: '准备方法参数dto'
        def map = new HashMap<String, Object>()
        map.put("name", "Amy")
        map.put("age", 11)
        def dto = new ScheduleTask()
        dto.setParams(map)
        and: 'mock'
        mockMethodMapper.selectByPrimaryKey(_) >> {
            return new QuartzMethodDTO(code: "code", level: "site", params: "[{\"name\",\"name\",\"defaultValue\":\"zh\",\"type\":\"String\",\"description\":\"\"},{\"name\":\"age\",\"defaultValue\":null,\"type\":\"Integer\",\"description\":\"年龄\"}]")
        }
        when: '方法调用'
        scheduleTaskService.create(dto, "site", 0L)
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
        Role roleDTO = new Role()
        roleDTO.setId(1L)
        roleDTO.setLevel("site")
        roleDTO.setName("name")
        roleDTO.setCode("code")
        roleDTO.setEnabled(true)
        def dto = new ScheduleTask()
        dto.setParams(map)
        dto.setMethodId(1L)
        ScheduleTask.NotifyUser user = new ScheduleTask.NotifyUser()
        user.setAdministrator(true)
        user.setAssigner(true)
        user.setCreator(true)
        dto.setNotifyUser(user)
        Long[] ids = new Long[1]
        ids[0] = 1L
        dto.setAssignUserIds(ids)
        def params = "[" +
                "{\"name\":\"name\",\"defaultValue\":\"zh\",\"type\":\"String\",\"description\":\"字符串\"}," +
                "{\"name\":\"age\",\"defaultValue\":null,\"type\":\"Integer\",\"description\":\"整型\"}," +
                "{\"name\":\"bool\",\"defaultValue\":true,\"type\":\"Boolean\",\"description\":\"布尔类型\"}," +
                "{\"name\":\"long\",\"defaultValue\":1,\"type\":\"Long\",\"description\":\"Long类型\"}," +
                "{\"name\":\"double\",\"defaultValue\":1.1,\"type\":\"Double\",\"description\":\"双精度浮点\"}" +
                "]"

        and: 'mock'
        mockMethodMapper.selectByPrimaryKey(_) >> {
            return new QuartzMethodDTO(code: "code", level: "site", params: params)
        }
        mockTaskMapper.insertSelective(_) >> { return 1 }
        iamFeignClient.queryByCode(_) >> new ResponseEntity<Role>(roleDTO, HttpStatus.OK)
        when: '方法调用'
        scheduleTaskService.create(dto, "site", 0L)
        then: '结果分析'
        def e = thrown(CommonException)
        e.message == "error.quartzTaskMember.create"
    }

    def "Enable"() {
        given: '参数准备'
        def id = 1L
        def objectVersionNumber = 1L

        and: 'mock'
        mockTaskMapper.selectByPrimaryKey(_) >> {
            return new QuartzTaskDTO(id: 1L, status: 'DISABLE', sourceId: 0L, level: "site", cronExpression: "1 * * * * ?", triggerType: "cron-trigger")
        }
        mockTaskMapper.updateByPrimaryKey(_) >> { return 1 }
        mockInstanceMapper.selectLastInstance(_) >> { return new QuartzTaskInstanceDTO() }
        mockInstanceMapper.updateByPrimaryKeySelective(_) >> { return 1 }

        when: '方法调用'
        scheduleTaskService.enable(id, objectVersionNumber, "site", 0L)
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
        scheduleTaskService.enable(id, objectVersionNumber, "site", 0L)
        then: '结果分析'
        def error = thrown(CommonException)
        error.message == errormsg
        where: '异常分析'
        dto                                                                    || errormsg
        null                                                                   || "error.scheduleTask.taskNotExist"
        new QuartzTaskDTO(id: 1L, status: 'DISABLE', sourceId: 0L, level: "site") || "error.scheduleTask.enableTaskFailed"

    }

    def "Disable"() {
        given: '参数准备'
        def id = 1L
        def objectVersionNumber = 1L

        and: 'mock'
        mockTaskMapper.selectByPrimaryKey(_) >> { return new QuartzTaskDTO(id: 1L, status: 'ENABLE', level: "site") }
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
        new QuartzTaskDTO(id: 1L, status: 'ENABLE', objectVersionNumber: 1L) || "error.scheduleTask.disableTaskFailed"
    }

    def "Delete"() {
        given: '参数准备'
        def id = 1L

        and: 'mock'
        mockTaskMapper.selectByPrimaryKey(_) >> {
            return new QuartzTaskDTO(id: 1L, status: 'ENABLE', sourceId: 0L, level: "site", objectVersionNumber: 1L)
        }
        mockTaskMapper.deleteByPrimaryKey(_) >> { return 1 }

        when: '方法调用'
        scheduleTaskService.delete(id, "site", 0L)
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
        scheduleTaskService.delete(id, "site", 0L)
        then: '结果分析'
        def error = thrown(CommonException)
        error.message == errormsg
        where: '异常分析'
        dto                                                                                            || errormsg
        null                                                                                           || "error.scheduleTask.taskNotExist"
        new QuartzTaskDTO(id: 1L, status: 'ENABLE', sourceId: 0L, level: "site", objectVersionNumber: 1L) || "error.scheduleTask.deleteTaskFailed"
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
        and: "mock"
        mockTaskMapper.fulltextSearch(_, _, _, _, _, _) >> { return taskList }
        when: '调用方法'
        PageRequest pageRequest = new PageRequest(1, 20)
        scheduleTaskService.pageQuery(pageRequest, status, name, description, params, "site", 0L)
        then: '无异常抛出'
        noExceptionThrown()
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
        dto                           | num
        null                          | 0
        new QuartzTaskDTO(level: "site") | 1
        new QuartzTaskDTO(level: "site") | 0
    }

    def "GetTaskDetail[Exception]"() {
        given: "参数准备"
        def id = 1L
        and: "mock"
        mockTaskMapper.selectByPrimaryKey(_) >> { return null }
        when: '方法调用'
        scheduleTaskService.getTaskDetail(id, "site", 0L)
        then: "结果分析"
        def e = thrown(CommonException)
        e.message == "error.scheduleTask.taskNotExist"
    }

    def "GetTaskDetail[lastInstance!=null]"() {
        given: "参数准备"
        def id = 1L

        def detail = new QuartzTaskDetail()
        detail.setId(1L)
        detail.setLevel("site")
        detail.setSourceId(0L)
        detail.setParams("{\"isInstantly\",true,\"name\",\"zh\",\"age\",20}")
        and: "mock"
        mockTaskMapper.selectByPrimaryKey(_) >> {
            return new QuartzTaskDTO(startTime: new Date(), level: "site", sourceId: 0L)
        }
        mockInstanceMapper.selectLastInstance(_) >> { return null }
        mockTaskMapper.selectTaskById(_) >> { return detail }
        when: '方法调用'
        scheduleTaskService.getTaskDetail(id, "site", 0L)
        then: "结果分析"
        def e = thrown(CommonException)
        e.message == "error.scheduleTaskDetailDTO.jsonIOException"
    }

    def "GetTaskDetail"() {
        given: "参数准备"
        def id = 1L

        def detail = new QuartzTaskDetail()
        detail.setId(1L)
        detail.setLevel("site")
        detail.setSourceId(0L)
        detail.setParams("{\"isInstantly\":true,\"name\":\"zh\",\"age\":20}")
        and: "mock"
        mockTaskMapper.selectByPrimaryKey(_) >> {
            return new QuartzTaskDTO(startTime: new Date(), sourceId: 0L, level: "site")
        }
        mockInstanceMapper.selectLastInstance(_) >> {
            return new QuartzTaskInstanceDTO(actualStartTime: new Date(), plannedNextTime: new Date(), level: "site", sourceId: 0L)
        }
        mockTaskMapper.selectTaskById(_) >> { return detail }
        when: '方法调用'
        scheduleTaskService.getTaskDetail(id, "site", 0L)
        then: "结果分析"
        noExceptionThrown()
    }

    def "CheckName"() {
        given: "参数准备"
        def name = "name"
        def list = new ArrayList<Long>()
        list.add(1L)

        and: "mock"
        mockTaskMapper.selectTaskIdByName(name, _, _) >> { return new ArrayList<Long>() }

        when: "方法调用"
        scheduleTaskService.checkName(name, "site", 0L)
        then: "抛出异常"
        noExceptionThrown()
    }

    def "CheckName[Exception]"() {
        given: "参数准备"
        def name = "name"
        def list = new ArrayList<Long>()
        list.add(1L)

        and: "mock"
        mockTaskMapper.selectTaskIdByName(name, ResourceLevel.SITE.value(), _) >> { return list }

        when: "方法调用"
        scheduleTaskService.checkName(name, ResourceLevel.SITE.value(), 0L)
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
        mockMethodMapper.select(_) >> { return new ArrayList<QuartzMethodDTO>() }
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
            def methods = new ArrayList<QuartzMethodDTO>()
            def method = new QuartzMethodDTO()
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
