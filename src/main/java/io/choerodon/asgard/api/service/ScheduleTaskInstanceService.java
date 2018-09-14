package io.choerodon.asgard.api.service;

import java.util.Set;

import org.springframework.http.ResponseEntity;

import io.choerodon.asgard.UpdateTaskInstanceStatusDTO;
import io.choerodon.asgard.api.dto.ScheduleTaskInstanceDTO;
import io.choerodon.asgard.api.dto.ScheduleTaskInstanceLogDTO;
import io.choerodon.asgard.schedule.dto.ScheduleInstanceConsumerDTO;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

public interface ScheduleTaskInstanceService {

    ResponseEntity<Page<ScheduleTaskInstanceDTO>> pageQuery(PageRequest pageRequest, String status,
                                                            String taskName, String exceptionMessage, String params);


    Set<ScheduleInstanceConsumerDTO> pollBatch(Set<String> methods, String instance);

    void updateStatus(UpdateTaskInstanceStatusDTO statusDTO);

    void unlockByInstance(String instance);

    Page<ScheduleTaskInstanceLogDTO> pagingQueryByTaskId(PageRequest pageRequest, Long taskId, String status,
                                                         String serviceInstanceId, String params);

    void failed(Long id,String exceptionMsg);
}
