package io.choerodon.asgard.api.service;

import io.choerodon.asgard.api.dto.ScheduleTaskInstanceDTO;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.springframework.http.ResponseEntity;

public interface ScheduleTaskInstanceService {

    ResponseEntity<Page<ScheduleTaskInstanceDTO>> pageQuery(PageRequest pageRequest, String status,
                                                            String taskName, String exceptionMessage, String params);


}
