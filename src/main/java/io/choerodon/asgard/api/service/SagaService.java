package io.choerodon.asgard.api.service;

import com.github.pagehelper.PageInfo;
import io.choerodon.asgard.api.dto.SagaDTO;
import io.choerodon.asgard.api.dto.SagaWithTaskDTO;
import io.choerodon.asgard.domain.Saga;
import org.springframework.http.ResponseEntity;

public interface SagaService {

    void create(Saga saga);

    ResponseEntity<PageInfo<SagaDTO>> pagingQuery(int page, int size, String code,
                                                  String description, String service, String params);

    ResponseEntity<SagaWithTaskDTO> query(Long id);

    void delete(Long id);

}
