package io.choerodon.asgard.infra.mapper;

import org.apache.ibatis.annotations.Param;
import org.springframework.security.core.parameters.P;

import java.util.*;

import io.choerodon.asgard.api.vo.*;
import io.choerodon.asgard.infra.dto.*;
import io.choerodon.mybatis.common.*;

public interface SagaInstanceMapper extends Mapper<SagaInstanceDTO> {

    List<SagaInstanceDetails> fulltextSearchInstance(@Param("sagaCode") String sagaCode,
                                              @Param("status") String status,
                                              @Param("refType") String refType,
                                              @Param("refId") String refId,
                                              @Param("params") String params,
                                              @Param("level") String level,
                                              @Param("sourceId") Long sourceId);

    Map<String, Integer> statisticsByStatus(@Param("level") String level,
                                            @Param("sourceId") Long sourceId);

    List<SagaInstanceFailureVO> statisticsFailure(@Param("level") String level,
                                                  @Param("sourceId") Long sourceId,
                                                  @Param("startTime") String startTime,
                                                  @Param("endTime") String endTime);

    List<SagaInstanceDTO> statisticsFailureList(@Param("level") String level,
                                                @Param("sourceId") Long sourceId,
                                                @Param("startTime") String startTime,
                                                @Param("endTime") String endTime);

    SagaInstanceDetails selectDetails(@Param("id") Long id);

    List<Map<String, Object>> selectFailedTimes(@Param("begin") String begin,
                                                @Param("end") String end);

    List<SagaInstanceDTO> selectUnConfirmedTimeOutInstance(@Param("timeOut") int unConfirmedTimeoutSeconds, @Param("now") Date now);

    List<Long> selectCompletedIdByDate(@Param("fromNowSeconds") long fromNowSeconds, @Param("now") Date now);

    int deleteBatchByIds(@Param("ids") List<Long> ids);
}
