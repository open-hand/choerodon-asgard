package io.choerodon.asgard.infra.feign;

import feign.Param;
import feign.RequestLine;
import io.choerodon.asgard.saga.dto.SagaStatusQueryDTO;

public interface StatusQueryFeign {

    @RequestLine("GET /choerodon/saga/{uuid}")
    SagaStatusQueryDTO getEventRecord(@Param("uuid") String uuid);


}
