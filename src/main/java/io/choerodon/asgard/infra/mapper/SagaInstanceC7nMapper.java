package io.choerodon.asgard.infra.mapper;

import io.choerodon.asgard.api.dto.SagaInstanceDTO;
import io.choerodon.asgard.api.vo.SagaInstanceFailureVO;
import io.choerodon.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * User: Mr.Wang
 * Date: 2020/4/22
 */
public interface SagaInstanceC7nMapper extends BaseMapper<SagaInstanceDTO> {
    List<SagaInstanceFailureVO> statisticsFailure(@Param("level") String level,
                                                  @Param("sourceId") Long sourceId,
                                                  @Param("startTime") String startTime,
                                                  @Param("endTime") String endTime);

    List<SagaInstanceDTO> statisticsFailureList(@Param("level") String level,
                                                @Param("sourceId") Long sourceId,
                                                @Param("startTime") String startTime,
                                                @Param("endTime") String endTime);
}
