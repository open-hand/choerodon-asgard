package io.choerodon.asgard.infra.mapper;

import io.choerodon.asgard.domain.SagaTask;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SagaTaskMapper extends Mapper<SagaTask> {

    List<SagaTask> selectNextSeqSagaTasks(@Param("sagaCode") String sagaCode, @Param("seq") int seq);

    List<SagaTask> selectFirstSeqSagaTasks(@Param("sagaCode") String sagaCode);

}
