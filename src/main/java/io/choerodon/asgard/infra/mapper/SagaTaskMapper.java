package io.choerodon.asgard.infra.mapper;

import io.choerodon.asgard.infra.dto.SagaTaskDTO;
import io.choerodon.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SagaTaskMapper extends BaseMapper<SagaTaskDTO> {

    List<SagaTaskDTO> selectNextSeqSagaTasks(@Param("sagaCode") String sagaCode, @Param("seq") int seq);

    List<SagaTaskDTO> selectFirstSeqSagaTasks(@Param("sagaCode") String sagaCode);

}
