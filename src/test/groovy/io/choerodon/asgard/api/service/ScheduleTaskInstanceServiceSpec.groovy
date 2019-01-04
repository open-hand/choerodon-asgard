package io.choerodon.asgard.api.service

import io.choerodon.asgard.IntegrationTestConfiguration
import io.choerodon.asgard.api.dto.ScheduleTaskInstanceDTO
import io.choerodon.asgard.api.service.impl.ScheduleTaskInstanceServiceImpl
import io.choerodon.asgard.common.UpdateStatusDTO
import io.choerodon.asgard.domain.QuartzTaskInstance
import io.choerodon.asgard.infra.mapper.QuartzTaskInstanceMapper
import io.choerodon.asgard.schedule.dto.ScheduleInstanceConsumerDTO
import io.choerodon.core.exception.FeignException
import io.choerodon.mybatis.pagehelper.domain.PageRequest
import io.choerodon.mybatis.pagehelper.domain.Sort
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class ScheduleTaskInstanceServiceSpec extends Specification {
    @Autowired
    private ScheduleTaskInstanceService scheduleTaskInstanceService

    private QuartzTaskInstanceMapper mockQuartzTaskInstanceMapper = Mock(QuartzTaskInstanceMapper)

    void setup() {
        scheduleTaskInstanceService = new ScheduleTaskInstanceServiceImpl(mockQuartzTaskInstanceMapper)
    }

    def "PageQuery"() {
        given: '参数准备'
        def status = "RUNNING"
        def taskName = "taskName"
        def exceptionMessage = "exceptionMsg"
        def params = "params"
        def level = "site"
        def sourceId = 0L
        def stiDTOList = new ArrayList<ScheduleTaskInstanceDTO>()
        def stiDTO = new ScheduleTaskInstanceDTO()
        stiDTO.setId(1L)
        stiDTOList.add(stiDTO)

        and: "构造pageRequest"
        def order = new Sort.Order("id")
        def pageRequest = new PageRequest(1, 20, new Sort(order))

        when: '方法调用'
        scheduleTaskInstanceService.pageQuery(pageRequest, status, taskName, exceptionMessage, params, level, sourceId)
        then: '参数准备'
        noExceptionThrown()
        1 * mockQuartzTaskInstanceMapper.fulltextSearch(status, taskName, exceptionMessage, params, level, sourceId)
    }


    def "UpdateStatus[Exception]"() {
        given: 'mock'
        mockQuartzTaskInstanceMapper.selectByPrimaryKey(_) >> { return dbInstance }
        mockQuartzTaskInstanceMapper.updateByPrimaryKeySelective(_) >> { return num }
        when: '方法调用'
        scheduleTaskInstanceService.updateStatus(dto)
        then: '抛出异常'
        def error = thrown(exceptionType)
        error.message == errorMsg
        where: '异常比对'
        dto                                                                                                                       | dbInstance                                                        | num || exceptionType  | errorMsg
        new UpdateStatusDTO(objectVersionNumber: null)                                                                | null                                                              | 0   || FeignException | "error.scheduleTaskInstanceService.updateStatus.objectVersionNumberNull"
        new UpdateStatusDTO(objectVersionNumber: 1L)                                                                  | null                                                              | 0   || FeignException | "error.scheduleTaskInstanceService.updateStatus.instanceNotExist"
        new UpdateStatusDTO(objectVersionNumber: 1L, status: 'COMPLETED', output: "1")                                | new QuartzTaskInstance(id: 1L, status: "FAILED")                  | 0 || FeignException | "error.scheduleTaskInstanceService.updateStatus.instanceWasFailed"
        new UpdateStatusDTO(objectVersionNumber: 1L, status: 'COMPLETED', output: "1")                                | new QuartzTaskInstance(id: 1L)                                    | 0 || FeignException | "error.scheduleTaskInstanceService.updateCompleteStatusFailed"
        new UpdateStatusDTO(objectVersionNumber: 1L, status: 'FAILED', output: "1")                                   | new QuartzTaskInstance(id: 1L, retriedCount: 1, maxRetryCount: 2) | 0 || FeignException | "error.scheduleTaskInstanceService.updateFailedStatusFailed"
        new UpdateStatusDTO(objectVersionNumber: 1L, status: 'FAILED', output: "1", exceptionMessage: "exceptionMsg") | new QuartzTaskInstance(id: 1L, retriedCount: 2, maxRetryCount: 1) | 0 || FeignException | "error.scheduleTaskInstanceService.updateFailedStatusFailed"
    }

    def "UpdateStatus"() {
        given: 'mock'
        mockQuartzTaskInstanceMapper.selectByPrimaryKey(_) >> {
            return new QuartzTaskInstance(id: 1L, retriedCount: 2, maxRetryCount: 1)
        }
        mockQuartzTaskInstanceMapper.updateByPrimaryKeySelective(_) >> { return 1 }
        when: '方法调用'
        scheduleTaskInstanceService.updateStatus(new UpdateStatusDTO(objectVersionNumber: 1L, status: 'FAILED', output: "1", exceptionMessage: "exceptionMsg"))
        then: '抛出异常'
        noExceptionThrown()
    }

    def "UnlockByInstance"() {
        given: '参数准备'
        def instance = "instance"
        and: 'mock'
        mockQuartzTaskInstanceMapper.unlockByInstance(_) >> { return 1 }
        when: '方法调用'
        scheduleTaskInstanceService.unlockByInstance(instance)
        then: "无异常抛出"
        noExceptionThrown()
    }

    def "PagingQueryByTaskId"() {
        given: '参数准备'
        def taskId = 1L
        def status = "status"
        def serviceInstanceId = "serviceInstanceId"
        def params = "params"

        and: "构造pageRequest"
        def order = new Sort.Order("id")
        def pageRequest = new PageRequest(0, 20, new Sort(order))

        when: '方法调用'
        scheduleTaskInstanceService.pagingQueryByTaskId(pageRequest, taskId, status, serviceInstanceId, params, "site", 0L)
        then: "无异常抛出"
        noExceptionThrown()
        1 * mockQuartzTaskInstanceMapper.selectByTaskId(_, _, _, _, _, _)
    }

    def "Failed"() {
        given: '参数准备'
        def id = 1L
        def exceptionMsg = "exceptionMsg"
        and: "mock"
        mockQuartzTaskInstanceMapper.selectByPrimaryKey(_) >> { return dto }
        mockQuartzTaskInstanceMapper.updateByPrimaryKey(_) >> { return num }
        when: "方法调用"
        scheduleTaskInstanceService.failed(id, exceptionMsg)
        then: "异常判断"
        noExceptionThrown()
        where: "异常比对"
        dto                            | num
        null                           | 0
        new QuartzTaskInstance(id: 1L) | 1
        new QuartzTaskInstance(id: 1L) | 0
    }
}
