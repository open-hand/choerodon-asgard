package io.choerodon.asgard.app.eventhandler;

import io.choerodon.asgard.app.service.RegisterInstanceService;
import org.hzero.register.event.event.InstanceAddedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class EurekaEventObserver implements ApplicationListener<InstanceAddedEvent> {
    private RegisterInstanceService registerInstanceService;

    public EurekaEventObserver(RegisterInstanceService registerInstanceService) {
        this.registerInstanceService = registerInstanceService;
    }

    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(InstanceAddedEvent event) {
        this.registerInstanceService.instanceUpConsumer(event);
    }

}
