package io.choerodon.asgard.app.service;

import io.choerodon.asgard.property.PropertyData;
import org.hzero.register.event.event.InstanceAddedEvent;
import org.springframework.cloud.client.ServiceInstance;

public interface RegisterInstanceService {

    void instanceDownConsumer(final InstanceAddedEvent payload);

    void instanceUpConsumer(final InstanceAddedEvent payload);

    void updateConsumer(final ServiceInstance instance);

    void propertyDataConsume(PropertyData propertyData, String version);
}
