package io.choerodon.asgard.api.service;

import io.choerodon.asgard.api.dto.RegisterInstancePayloadDTO;

public interface RegisterInstanceService {

    void msgConsumer(final RegisterInstancePayloadDTO payload, final boolean up);

}
