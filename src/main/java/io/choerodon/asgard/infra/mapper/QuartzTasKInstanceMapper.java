package io.choerodon.asgard.infra.mapper;

import io.choerodon.asgard.domain.QuartzTasKInstance;
import io.choerodon.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

public interface QuartzTasKInstanceMapper extends BaseMapper<QuartzTasKInstance> {

    Date selectLastTime(@Param("taskId") long taskId);

    int countUnCompletedInstance(@Param("taskId") long taskId);

}
