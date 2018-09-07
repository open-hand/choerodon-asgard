package io.choerodon.asgard.api.service;

import io.choerodon.asgard.api.dto.ScheduleTaskDTO;
import io.choerodon.asgard.domain.QuartzTask;

public interface ScheduleTaskService {

    QuartzTask create(ScheduleTaskDTO dto);

    void enable(long id, long objectVersionNumber);

    void disable(long id, Long objectVersionNumber, boolean executeWithIn);

    void delete(long id);

}
