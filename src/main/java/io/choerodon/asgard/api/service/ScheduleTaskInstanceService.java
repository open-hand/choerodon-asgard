package io.choerodon.asgard.api.service;

import com.github.pagehelper.PageInfo;
import io.choerodon.asgard.api.dto.PollScheduleTaskInstanceDTO;
import io.choerodon.asgard.api.dto.ScheduleTaskInstanceDTO;
import io.choerodon.asgard.api.dto.ScheduleTaskInstanceLogDTO;
import io.choerodon.asgard.common.UpdateStatusDTO;
import io.choerodon.asgard.schedule.dto.PollScheduleInstanceDTO;
import org.springframework.http.ResponseEntity;

import java.util.Set;

public interface ScheduleTaskInstanceService {

    ResponseEntity<PageInfo<ScheduleTaskInstanceDTO>> pageQuery(int page, int size, String status,
                                                                String taskName, String exceptionMessage,
                                                                String params, String level, Long sourceId);


    Set<PollScheduleTaskInstanceDTO> pollBatch(PollScheduleInstanceDTO dto);

    void updateStatus(UpdateStatusDTO statusDTO);

    void unlockByInstance(String instance);

    PageInfo<ScheduleTaskInstanceLogDTO> pagingQueryByTaskId(int page, int size, Long taskId,
                                                             String status, String serviceInstanceId,
                                                             String params, String level, Long sourceId);

    void failed(Long id, String exceptionMsg);
}
