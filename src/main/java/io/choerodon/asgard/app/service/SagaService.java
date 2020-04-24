package io.choerodon.asgard.app.service;

import com.github.pagehelper.PageInfo;
import io.choerodon.asgard.api.vo.Saga;
import io.choerodon.asgard.api.vo.SagaWithTask;
import io.choerodon.asgard.infra.dto.SagaDTO;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

public interface SagaService {

    void create(SagaDTO saga);

    ResponseEntity<Page<Saga>> pagingQuery(PageRequest pageRequest, String code, String description, String service, String params);

    ResponseEntity<SagaWithTask> query(Long id);

    void delete(Long id);

}
