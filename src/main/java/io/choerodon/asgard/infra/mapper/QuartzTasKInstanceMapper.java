package io.choerodon.asgard.infra.mapper;

import io.choerodon.asgard.api.dto.ScheduleTaskInstanceDTO;
import io.choerodon.asgard.domain.QuartzTasKInstance;
import io.choerodon.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface QuartzTasKInstanceMapper extends BaseMapper<QuartzTasKInstance> {

    Date selectLastTime(@Param("taskId") long taskId);

    int countUnCompletedInstance(@Param("taskId") long taskId);

    List<ScheduleTaskInstanceDTO> fulltextSearch(@Param("status") String status,
                                                 @Param("taskName") String taskName,
                                                 @Param("exceptionMessage") String exceptionMessage,
                                                 @Param("params") String params);


}
