package io.choerodon.asgard.infra.feign;

import feign.Param;
import feign.RequestLine;

public interface StatusQueryFeign {

    @RequestLine("GET /choerodon/saga/{uuid}")
    String getEventRecord(@Param("uuid") String uuid);


}
