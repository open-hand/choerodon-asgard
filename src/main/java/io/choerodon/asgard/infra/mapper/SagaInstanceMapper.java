package io.choerodon.asgard.infra.mapper;

import io.choerodon.asgard.api.dto.SagaInstanceDTO;
import io.choerodon.asgard.api.dto.SagaInstanceDetailsDTO;
import io.choerodon.asgard.domain.SagaInstance;
import io.choerodon.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface SagaInstanceMapper extends BaseMapper<SagaInstance> {

    List<SagaInstanceDTO> fulltextSearchInstance(@Param("sagaCode") String sagaCode,
                                                 @Param("status") String status,
                                                 @Param("refType") String refType,
                                                 @Param("refId") String refId,
                                                 @Param("params") String params,
                                                 @Param("level") String level,
                                                 @Param("sourceId") Long sourceId);

    Map<String, Integer> statisticsByStatus(@Param("level") String level,
                                            @Param("sourceId") Long sourceId);

    SagaInstanceDetailsDTO selectDetails(@Param("id") Long id);

    List<Map<String, Object>> selectFailedTimes(@Param("begin") String begin,
                                                @Param("end") String end);

    List<SagaInstance> selectUnConfirmedTimeOutInstance(@Param("timeOut") int unConfirmedTimeoutSeconds, @Param("now") Date now);

    List<Long> selectCompletedIdByDate(@Param("fromNowSeconds") long fromNowSeconds, @Param("now") Date now);

    int deleteBatchByIds(@Param("ids") List<Long> ids);
}
