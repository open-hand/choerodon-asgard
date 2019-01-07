package io.choerodon.asgard.api.service;

import java.util.List;
import java.util.Set;

import org.springframework.http.ResponseEntity;

import io.choerodon.asgard.api.dto.SagaTaskInstanceDTO;
import io.choerodon.asgard.api.dto.SagaTaskInstanceInfoDTO;
import io.choerodon.asgard.api.dto.SagaTaskInstanceStatusDTO;
import io.choerodon.asgard.domain.SagaTaskInstance;
import io.choerodon.asgard.saga.dto.PollSagaTaskInstanceDTO;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

public interface SagaTaskInstanceService {

    Set<SagaTaskInstanceDTO> pollBatch(final PollSagaTaskInstanceDTO pollBatchDTO);

    void updateStatus(SagaTaskInstanceStatusDTO statusDTO);

    SagaTaskInstanceDTO query(long id);

    void unlockByInstance(String instance);

    void retry(long id);

    void unlockById(long id);

    void forceFailed(long id);

    ResponseEntity<Page<SagaTaskInstanceInfoDTO>> pageQuery(PageRequest pageRequest, String sagaInstanceCode,
                                                            String status, String taskInstanceCode,
                                                            String params, String level, Long sourceId);

    /**
     * 根据事务实例Id查出某一seq下的全部任务实例
     *
     * @param sagaInatanceId 事务实例Id
     * @param seq            序列号
     * @return
     */
    List<SagaTaskInstance> queryByInstanceIdAndSeq(Long sagaInatanceId, Integer seq);
}
