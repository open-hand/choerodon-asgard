package io.choerodon.asgard.infra.mapper;

import io.choerodon.asgard.domain.SagaTask;
import io.choerodon.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SagaTaskMapper extends BaseMapper<SagaTask> {

    List<SagaTask> selectNextSeqSagaTasks(@Param("sagaCode") String sagaCode, @Param("seq") int seq);

}
