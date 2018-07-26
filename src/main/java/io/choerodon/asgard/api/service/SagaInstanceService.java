package io.choerodon.asgard.api.service;

import io.choerodon.asgard.api.dto.SagaInstanceDTO;
import io.choerodon.asgard.api.dto.SagaWithTaskInstanceDTO;
import io.choerodon.asgard.api.dto.StartInstanceDTO;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.springframework.http.ResponseEntity;

public interface SagaInstanceService {

    ResponseEntity<SagaInstanceDTO> start(StartInstanceDTO dto);

    ResponseEntity<Page<SagaInstanceDTO>> pageQuery(PageRequest pageRequest, String sagaCode,
                                                    String status, String refType,
                                                    String refId, String params);

    ResponseEntity<SagaWithTaskInstanceDTO> query(Long id);

}