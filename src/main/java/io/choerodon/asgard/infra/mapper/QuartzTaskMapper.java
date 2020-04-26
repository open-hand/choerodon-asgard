package io.choerodon.asgard.infra.mapper;

import io.choerodon.asgard.api.vo.QuartzTaskDetail;
import io.choerodon.asgard.infra.dto.QuartzTaskDTO;
import io.choerodon.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface QuartzTaskMapper extends BaseMapper<QuartzTaskDTO> {

    @Select("SELECT id FROM asgard_quartz_task WHERE name = #{taskName} AND fd_level = #{level} AND source_id= #{sourceId}")
    List<Long> selectTaskIdByName(@Param("taskName") String taskName, @Param("level") String level, @Param("sourceId") Long sourceId);

    List<QuartzTaskDTO> fulltextSearch(@Param("status") String status,
                                       @Param("name") String name,
                                       @Param("description") String description,
                                       @Param("params") String params,
                                       @Param("level") String level,
                                       @Param("sourceId") Long sourceId);

    QuartzTaskDetail selectTaskById(@Param("id") Long id);
}
