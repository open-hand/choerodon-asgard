package io.choerodon.asgard.infra.mapper;

import io.choerodon.asgard.api.dto.ScheduleTaskInstanceDTO;
import io.choerodon.asgard.domain.QuartzTasKInstance;
import io.choerodon.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface QuartzTaskInstanceMapper extends BaseMapper<QuartzTasKInstance> {

    QuartzTasKInstance selectLastInstance(@Param("taskId") long taskId);

    List<ScheduleTaskInstanceDTO> fulltextSearch(@Param("status") String status,
                                                 @Param("taskName") String taskName,
                                                 @Param("exceptionMessage") String exceptionMessage,
                                                 @Param("params") String params);


}
