package io.choerodon.asgard.app.service;

import com.github.pagehelper.PageInfo;
import io.choerodon.asgard.api.vo.Saga;
import io.choerodon.asgard.api.vo.SagaSearchVO;
import io.choerodon.asgard.api.vo.SagaWithTask;
import io.choerodon.asgard.infra.dto.SagaDTO;
import org.springframework.http.ResponseEntity;

public interface SagaService {

    void create(SagaDTO saga);

    ResponseEntity<PageInfo<Saga>> pagingQuery(int page, int size, SagaSearchVO sagaSearchVO);

    ResponseEntity<SagaWithTask> query(Long id);

    void delete(Long id);

}
