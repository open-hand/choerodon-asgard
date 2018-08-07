package io.choerodon.asgard.infra.mapper;

import io.choerodon.asgard.api.dto.SagaTaskInstanceDTO;
import io.choerodon.asgard.domain.SagaTaskInstance;
import io.choerodon.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Set;

public interface SagaTaskInstanceMapper extends BaseMapper<SagaTaskInstance> {

    Set<SagaTaskInstanceDTO> pollBatchNoneLimit(@Param("sagaCode") String sagaCode,
                                                @Param("taskCode") String taskCode,
                                                @Param("instance") String instance);

    List<SagaTaskInstanceDTO> pollBatchTypeAndIdLimit(@Param("sagaCode") String sagaCode,
                                                      @Param("taskCode") String taskCode);

    List<SagaTaskInstanceDTO> pollBatchTypeLimit(@Param("sagaCode") String sagaCode,
                                                 @Param("taskCode") String taskCode);


    int lockByInstanceAndUpdateStartTime(@Param("id") long id,
                                         @Param("instance") String instance,
                                         @Param("number") Long objectVersionNumber,
                                         @Param("time") Date date);

    void increaseRetriedCount(@Param("id") long id);

    void unlockByInstance(@Param("instance") String instance);

    List<SagaTaskInstanceDTO> selectAllBySagaInstanceId(@Param("sagaInstanceId") Long instanceId);

}
