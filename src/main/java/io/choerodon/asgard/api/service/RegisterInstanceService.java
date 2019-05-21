package io.choerodon.asgard.api.service;

import io.choerodon.asgard.property.PropertyData;
import io.choerodon.eureka.event.EurekaEventPayload;

public interface RegisterInstanceService {

    void instanceDownConsumer(final EurekaEventPayload payload);

    void instanceUpConsumer(final EurekaEventPayload payload);

    void propertyDataConsume(PropertyData propertyData, String version);
}
