package io.choerodon.asgard.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.asgard.api.dto.ScheduleMethodParamsDTO;
import io.choerodon.asgard.domain.QuartzMethod;
import io.choerodon.mybatis.common.BaseMapper;

public interface QuartzMethodMapper extends BaseMapper<QuartzMethod> {

    List<QuartzMethod> fulltextSearch(@Param("code") String code,
                                      @Param("service") String service,
                                      @Param("method") String method,
                                      @Param("description") String description,
                                      @Param("params") String params,
                                      @Param("level") String level);


    List<QuartzMethod> selectByService(@Param("service") String service,
                                       @Param("level") String level);

    ScheduleMethodParamsDTO selectParamsById(@Param("id") Long id);


}
