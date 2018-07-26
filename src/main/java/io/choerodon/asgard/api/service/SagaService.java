package io.choerodon.asgard.api.service;

import io.choerodon.asgard.api.dto.SagaDTO;
import io.choerodon.asgard.api.dto.SagaWithTaskDTO;
import io.choerodon.asgard.domain.Saga;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.springframework.http.ResponseEntity;

public interface SagaService {

    void createSaga(Saga saga);

    ResponseEntity<Page<SagaDTO>> pagingQuery (PageRequest pageRequest, String code,
                                               String description, String service, String params);

    ResponseEntity<SagaWithTaskDTO> query(Long id);
}
