package io.choerodon.asgard.infra.mapper;

import io.choerodon.asgard.domain.QuartzTask;
import io.choerodon.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface QuartzTaskMapper extends BaseMapper<QuartzTask> {

    @Select("SELECT id FROM asgard_quartz_task WHERE name = #{taskName}")
    List<Long> selectTaskIdByName(@Param("taskName") String taskName);

    List<QuartzTask> fulltextSearch(@Param("status") String status,
                                    @Param("name") String name,
                                    @Param("description") String description,
                                    @Param("params") String params);


}
