package io.choerodon.asgard.infra.mapper;

import io.choerodon.asgard.api.dto.SagaTaskInstanceDTO;
import io.choerodon.asgard.domain.SagaTaskInstance;
import io.choerodon.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SagaTaskInstanceMapper extends BaseMapper<SagaTaskInstance> {

    List<SagaTaskInstanceDTO> pollBatchNoneLimit(@Param("sagaCode") String sagaCode,
                                        @Param("taskCode") String taskCode,
                                        @Param("instance") String instance);


    List<SagaTaskInstanceDTO> pollBatchTypeAndIdLimit(@Param("sagaCode") String sagaCode,
                                                 @Param("taskCode") String taskCode);

    List<SagaTaskInstanceDTO> pollBatchTypeLimit(@Param("sagaCode") String sagaCode,
                                                      @Param("taskCode") String taskCode);


    int lockByInstance(@Param("id") long id, @Param("instance") String instance);

    void increaseRetriedCount(@Param("id") long id);

    void unlockByInstance(@Param("instance") String instance);

    List<SagaTaskInstanceDTO> selectAllBySagaInstanceId(@Param("sagaInstanceId") Long instanceId);

}
