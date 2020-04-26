package io.choerodon.asgard.app.service;

import io.choerodon.asgard.property.PropertyData;
import org.hzero.register.event.event.InstanceAddedEvent;

public interface RegisterInstanceService {

    void instanceDownConsumer(final InstanceAddedEvent payload);

    void instanceUpConsumer(final InstanceAddedEvent payload);

    void propertyDataConsume(PropertyData propertyData, String version);
}
