package io.choerodon.asgard.api.service;

import io.choerodon.asgard.api.dto.RegisterInstancePayloadDTO;

public interface RegisterInstanceService {

    void instanceDownConsumer(final RegisterInstancePayloadDTO payload);

    void instanceUpConsumer(final RegisterInstancePayloadDTO payload);

}
