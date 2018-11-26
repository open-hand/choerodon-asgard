package io.choerodon.asgard.api.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import io.choerodon.asgard.api.dto.ScheduleMethodDTO;
import io.choerodon.asgard.api.dto.ScheduleMethodInfoDTO;
import io.choerodon.asgard.api.dto.ScheduleMethodParamsDTO;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

public interface ScheduleMethodService {

    ResponseEntity<Page<ScheduleMethodInfoDTO>> pageQuery(PageRequest pageRequest, String code,
                                                          String service, String method, String description, String params, String level);

    List<ScheduleMethodDTO> getMethodByService(String serviceName, String level);


    ScheduleMethodParamsDTO getParams(Long id, String level);

    Long getMethodIdByCode(String code);
}
