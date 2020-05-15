package io.choerodon.asgard.app.service;


import io.choerodon.asgard.api.vo.PollScheduleTaskInstance;
import io.choerodon.asgard.api.vo.ScheduleTaskInstance;
import io.choerodon.asgard.api.vo.ScheduleTaskInstanceLog;
import io.choerodon.asgard.common.UpdateStatusDTO;
import io.choerodon.asgard.schedule.dto.PollScheduleInstanceDTO;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.springframework.http.ResponseEntity;

import java.util.Set;

public interface ScheduleTaskInstanceService {

    ResponseEntity<Page<ScheduleTaskInstance>> pageQuery(PageRequest pageRequest, String status,
                                                         String taskName, String exceptionMessage,
                                                         String params, String level, Long sourceId);


    Set<PollScheduleTaskInstance> pollBatch(PollScheduleInstanceDTO dto);

    void updateStatus(UpdateStatusDTO statusDTO);

    void unlockByInstance(String instance);

    Page<ScheduleTaskInstanceLog> pagingQueryByTaskId(PageRequest pageRequest, Long taskId,
                                                          String status, String serviceInstanceId,
                                                          String params, String level, Long sourceId);

    void failed(Long id, String exceptionMsg);
}
