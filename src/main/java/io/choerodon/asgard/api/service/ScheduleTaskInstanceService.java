package io.choerodon.asgard.api.service;

import io.choerodon.asgard.api.dto.PollScheduleTaskInstanceDTO;
import io.choerodon.asgard.api.dto.ScheduleTaskInstanceDTO;
import io.choerodon.asgard.api.dto.ScheduleTaskInstanceLogDTO;
import io.choerodon.asgard.common.UpdateStatusDTO;
import io.choerodon.asgard.schedule.dto.PollScheduleInstanceDTO;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.springframework.http.ResponseEntity;

import java.util.Set;

public interface ScheduleTaskInstanceService {

    ResponseEntity<Page<ScheduleTaskInstanceDTO>> pageQuery(PageRequest pageRequest, String status,
                                                            String taskName, String exceptionMessage,
                                                            String params, String level, Long sourceId);


    Set<PollScheduleTaskInstanceDTO> pollBatch(PollScheduleInstanceDTO dto);

    void updateStatus(UpdateStatusDTO statusDTO);

    void unlockByInstance(String instance);

    Page<ScheduleTaskInstanceLogDTO> pagingQueryByTaskId(PageRequest pageRequest, Long taskId,
                                                         String status, String serviceInstanceId,
                                                         String params, String level, Long sourceId);

    void failed(Long id, String exceptionMsg);
}
