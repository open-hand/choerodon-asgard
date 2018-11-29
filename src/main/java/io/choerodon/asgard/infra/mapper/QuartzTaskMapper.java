package io.choerodon.asgard.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import io.choerodon.asgard.api.dto.SystemNotificationDTO;
import io.choerodon.asgard.domain.QuartzTask;
import io.choerodon.asgard.domain.QuartzTaskDetail;
import io.choerodon.mybatis.common.BaseMapper;

public interface QuartzTaskMapper extends BaseMapper<QuartzTask> {

    @Select("SELECT id FROM asgard_quartz_task WHERE name = #{taskName} AND fd_level = #{level} AND source_id= #{sourceId}")
    List<Long> selectTaskIdByName(@Param("taskName") String taskName, @Param("level") String level, @Param("sourceId") Long sourceId);

    List<QuartzTask> fulltextSearch(@Param("status") String status,
                                    @Param("name") String name,
                                    @Param("description") String description,
                                    @Param("params") String params,
                                    @Param("level") String level,
                                    @Param("sourceId") Long sourceId);

    QuartzTaskDetail selectTaskById(@Param("id") Long id);

    List<SystemNotificationDTO> selectNotification(@Param("level") String level,
                                                   @Param("sourceId") Long sourceId,
                                                   @Param("code") String code,
                                                   @Param("status") String status,
                                                   @Param("content") String content,
                                                   @Param("params") String params);
}
