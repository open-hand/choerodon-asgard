package io.choerodon.asgard.infra.mapper;

import java.util.List;

import io.choerodon.asgard.api.vo.ScheduleMethodParams;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

import io.choerodon.asgard.infra.dto.QuartzMethodDTO;

public interface QuartzMethodMapper extends Mapper<QuartzMethodDTO> {

    List<QuartzMethodDTO> fulltextSearch(@Param("code") String code,
                                         @Param("service") String service,
                                         @Param("method") String method,
                                         @Param("description") String description,
                                         @Param("params") String params,
                                         @Param("level") String level);


    List<QuartzMethodDTO> selectByService(@Param("service") String service,
                                          @Param("level") String level);

    ScheduleMethodParams selectParamsById(@Param("id") Long id);


}
