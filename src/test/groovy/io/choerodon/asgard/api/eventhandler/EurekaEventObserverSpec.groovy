package io.choerodon.asgard.api.eventhandler

import io.choerodon.asgard.api.service.RegisterInstanceService
import io.choerodon.eureka.event.EurekaEventPayload
import spock.lang.Specification

class EurekaEventObserverSpec extends Specification {

    def "test receiveDownEvent"() {
        given: 'mock RegisterInstanceService'
        def service = Mock(RegisterInstanceService)
        def observer = new EurekaEventObserver(service)

        when:
        observer.receiveDownEvent(new EurekaEventPayload())
        then:
        1 * service.instanceDownConsumer(_)
    }
}
