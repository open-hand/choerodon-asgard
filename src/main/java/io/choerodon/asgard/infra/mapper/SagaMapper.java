package io.choerodon.asgard.infra.mapper;

import io.choerodon.asgard.api.dto.SagaDTO;
import io.choerodon.asgard.domain.Saga;
import io.choerodon.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SagaMapper extends BaseMapper<Saga> {

    @Select("select count(*) from asgard_orch_saga where code = #{code}")
    boolean existByCode(@Param("code") String code);

    List<SagaDTO> fulltextSearch(@Param("code") String code, @Param("description") String description,
                                 @Param("service") String service, @Param("params") String params);

}
