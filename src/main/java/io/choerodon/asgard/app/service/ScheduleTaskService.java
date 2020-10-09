package io.choerodon.asgard.app.service;

import io.choerodon.asgard.api.vo.QuartzTask;
import io.choerodon.asgard.api.vo.ScheduleTask;
import io.choerodon.asgard.api.vo.ScheduleTaskDetail;
import io.choerodon.asgard.infra.dto.QuartzTaskDTO;
import io.choerodon.asgard.property.PropertyTimedTask;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ScheduleTaskService {

    QuartzTaskDTO create(ScheduleTask dto, String level, Long sourceId);

    void enable(long id, long objectVersionNumber, String level, Long sourceId);

    void disable(long id, Long objectVersionNumber, boolean executeWithIn);

    void disableByLevelAndSourceId(String level, long sourceId);

    void delete(long id, String level, Long sourceId);

    void deleteByName(String name, String level, Long sourceId);

    void finish(long id);

    ResponseEntity<Page<QuartzTask>> pageQuery(PageRequest pageable, String status, String name, String description, String params, String level, Long sourceId);

    ScheduleTaskDetail getTaskDetail(Long id, String level, Long sourceId);

    void checkName(String name, String level, Long sourceId);

    void checkNameAllLevel(String name);

    /**
     * 自定义创建定时任务
     *
     * @param service
     * @param scanTasks
     */
    void createTaskList(final String service, final List<PropertyTimedTask> scanTasks, String version);

    /**
     * 根据id查询任务
     *
     * @param id
     * @param level
     * @return
     */
    QuartzTaskDTO getQuartzTask(long id, String level, Long sourceId);

    /**
     * 根据serviceCode和methodCode创建定时任务
     * @param dto
     * @param sourceLevel
     * @param sourceId
     * @return
     */
    QuartzTaskDTO createByServiceCodeAndMethodCode(ScheduleTask dto, String sourceLevel, Long sourceId);
}
