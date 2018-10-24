package io.choerodon.asgard.api.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import io.choerodon.asgard.api.dto.QuartzTaskDTO;
import io.choerodon.asgard.api.dto.ScheduleTaskDTO;
import io.choerodon.asgard.api.dto.ScheduleTaskDetailDTO;
import io.choerodon.asgard.domain.QuartzTask;
import io.choerodon.asgard.property.PropertyTimedTask;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

public interface ScheduleTaskService {

    QuartzTask create(ScheduleTaskDTO dto);

    void enable(long id, long objectVersionNumber);

    void disable(long id, Long objectVersionNumber, boolean executeWithIn);

    void delete(long id);

    void finish(long id);

    ResponseEntity<Page<QuartzTaskDTO>> pageQuery(PageRequest pageRequest, String status, String name, String description, String params);

    ScheduleTaskDetailDTO getTaskDetail(Long id);

    void checkName(String name);

    /**
     * 自定义创建定时任务
     * @param service
     * @param scanTasks
     */
    void createTaskList(final String service, final List<PropertyTimedTask> scanTasks,String version);

}
