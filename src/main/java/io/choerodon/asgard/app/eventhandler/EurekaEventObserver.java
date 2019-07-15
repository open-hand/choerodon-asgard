package io.choerodon.asgard.app.eventhandler;

import io.choerodon.asgard.app.service.RegisterInstanceService;
import io.choerodon.eureka.event.AbstractEurekaEventObserver;
import io.choerodon.eureka.event.EurekaEventPayload;
import org.springframework.stereotype.Component;

@Component
public class EurekaEventObserver extends AbstractEurekaEventObserver {

    private RegisterInstanceService registerInstanceService;

    public EurekaEventObserver(RegisterInstanceService registerInstanceService) {
        this.registerInstanceService = registerInstanceService;
    }

    @Override
    public void receiveUpEvent(EurekaEventPayload payload) {
        //registerInstanceService.instanceUpConsumer(payload); 使用Saga统一刷新数据了
    }

    @Override
    public void receiveDownEvent(EurekaEventPayload payload) {
        registerInstanceService.instanceDownConsumer(payload);
    }

}
