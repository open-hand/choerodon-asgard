package io.choerodon.asgard.infra.mapper;

import io.choerodon.asgard.api.dto.SagaInstanceDTO;
import io.choerodon.asgard.domain.SagaInstance;
import io.choerodon.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SagaInstanceMapper extends BaseMapper<SagaInstance> {

    List<SagaInstanceDTO> fulltextSearch(@Param("sagaCode") String sagaCode,
                                         @Param("status") String status,
                                         @Param("refType") String refType,
                                         @Param("refId") String refId,
                                         @Param("params") String params);

}
