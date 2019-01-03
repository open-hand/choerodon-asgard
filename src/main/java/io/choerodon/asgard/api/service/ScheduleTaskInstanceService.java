package io.choerodon.asgard.api.service;

import java.util.Set;

import io.choerodon.asgard.common.UpdateStatusDTO;
import org.springframework.http.ResponseEntity;

import io.choerodon.asgard.api.dto.ScheduleTaskInstanceDTO;
import io.choerodon.asgard.api.dto.ScheduleTaskInstanceLogDTO;
import io.choerodon.asgard.schedule.dto.ScheduleInstanceConsumerDTO;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

public interface ScheduleTaskInstanceService {

    ResponseEntity<Page<ScheduleTaskInstanceDTO>> pageQuery(PageRequest pageRequest, String status,
                                                            String taskName, String exceptionMessage,
                                                            String params, String level, Long sourceId);


    Set<ScheduleInstanceConsumerDTO> pollBatch(Set<String> methods, String instance);

    void updateStatus(UpdateStatusDTO statusDTO);

    void unlockByInstance(String instance);

    Page<ScheduleTaskInstanceLogDTO> pagingQueryByTaskId(PageRequest pageRequest, Long taskId,
                                                         String status, String serviceInstanceId,
                                                         String params, String level, Long sourceId);

    void failed(Long id, String exceptionMsg);
}
