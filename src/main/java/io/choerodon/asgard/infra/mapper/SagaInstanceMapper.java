package io.choerodon.asgard.infra.mapper;

import io.choerodon.asgard.api.vo.SagaInstance;
import io.choerodon.asgard.api.vo.SagaInstanceDetails;
import io.choerodon.asgard.infra.dto.SagaInstanceDTO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface SagaInstanceMapper extends Mapper<SagaInstanceDTO> {

    List<SagaInstance> fulltextSearchInstance(@Param("sagaCode") String sagaCode,
                                              @Param("status") String status,
                                              @Param("refType") String refType,
                                              @Param("refId") String refId,
                                              @Param("params") String params,
                                              @Param("level") String level,
                                              @Param("sourceId") Long sourceId);

    Map<String, Integer> statisticsByStatus(@Param("level") String level,
                                            @Param("sourceId") Long sourceId);

    SagaInstanceDetails selectDetails(@Param("id") Long id);

    List<Map<String, Object>> selectFailedTimes(@Param("begin") String begin,
                                                @Param("end") String end);

    List<SagaInstanceDTO> selectUnConfirmedTimeOutInstance(@Param("timeOut") int unConfirmedTimeoutSeconds, @Param("now") Date now);

    List<Long> selectCompletedIdByDate(@Param("fromNowSeconds") long fromNowSeconds, @Param("now") Date now);

    int deleteBatchByIds(@Param("ids") List<Long> ids);
}
