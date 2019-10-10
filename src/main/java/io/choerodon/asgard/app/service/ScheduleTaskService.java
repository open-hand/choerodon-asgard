package io.choerodon.asgard.app.service;

import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.asgard.infra.dto.QuartzTaskDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import io.choerodon.asgard.api.vo.QuartzTask;
import io.choerodon.asgard.api.vo.ScheduleTask;
import io.choerodon.asgard.api.vo.ScheduleTaskDetail;
import io.choerodon.asgard.property.PropertyTimedTask;

public interface ScheduleTaskService {

    QuartzTaskDTO create(ScheduleTask dto, String level, Long sourceId);

    void enable(long id, long objectVersionNumber, String level, Long sourceId);

    void disable(long id, Long objectVersionNumber, boolean executeWithIn);

    void disableByLevelAndSourceId(String level, long sourceId);

    void delete(long id, String level, Long sourceId);

    void finish(long id);

    ResponseEntity<PageInfo<QuartzTask>> pageQuery(Pageable pageable, String status, String name, String description, String params, String level, Long sourceId);

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

}
