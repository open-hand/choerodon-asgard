package io.choerodon.asgard.infra.mapper;

import io.choerodon.asgard.api.vo.PollScheduleTaskInstance;
import io.choerodon.asgard.api.vo.ScheduleTaskInstance;
import io.choerodon.asgard.api.vo.ScheduleTaskInstanceLog;
import io.choerodon.asgard.infra.dto.QuartzTaskInstanceDTO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface QuartzTaskInstanceMapper extends Mapper<QuartzTaskInstanceDTO> {

    QuartzTaskInstanceDTO selectLastInstance(@Param("taskId") long taskId);

    List<ScheduleTaskInstance> fulltextSearch(@Param("status") String status,
                                              @Param("taskName") String taskName,
                                              @Param("exceptionMessage") String exceptionMessage,
                                              @Param("params") String params,
                                              @Param("level") String level,
                                              @Param("sourceId") Long sourceId);

    List<PollScheduleTaskInstance> pollBathByMethod(@Param("method") String method);

    int unlockByInstance(@Param("instance") String instance);

    int lockByInstanceAndUpdateStartTime(@Param("id") long id,
                                         @Param("instance") String instance,
                                         @Param("number") Long objectVersionNumber,
                                         @Param("time") Date date);


    List<ScheduleTaskInstanceLog> selectByTaskId(@Param("taskId") Long taskId,
                                                 @Param("status") String status,
                                                 @Param("serviceInstanceId") String serviceInstanceId,
                                                 @Param("params") String params,
                                                 @Param("level") String level,
                                                 @Param("sourceId") Long sourceId);

}
