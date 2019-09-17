package io.choerodon.asgard.infra.mapper;

import java.util.List;

import io.choerodon.asgard.infra.dto.QuartzTaskDTO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import io.choerodon.asgard.api.vo.QuartzTaskDetail;

public interface QuartzTaskMapper extends Mapper<QuartzTaskDTO> {

    @Select("SELECT id FROM asgard_quartz_task WHERE name = #{taskName} AND fd_level = #{level} AND source_id= #{sourceId}")
    List<Long> selectTaskIdByName(@Param("taskName") String taskName, @Param("level") String level, @Param("sourceId") Long sourceId);

    List<QuartzTaskDTO> fulltextSearch(@Param("status") String status,
                                       @Param("name") String name,
                                       @Param("description") String description,
                                       @Param("param") String param,
                                       @Param("level") String level,
                                       @Param("sourceId") Long sourceId);

    QuartzTaskDetail selectTaskById(@Param("id") Long id);
}
