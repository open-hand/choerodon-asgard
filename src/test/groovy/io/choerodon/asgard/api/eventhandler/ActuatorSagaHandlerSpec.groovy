package io.choerodon.asgard.api.eventhandler

import io.choerodon.asgard.api.service.RegisterInstanceService
import spock.lang.Specification

class ActuatorSagaHandlerSpec extends Specification {

    def "test receiveDownEvent"() {
        given: 'mock RegisterInstanceService'
        def service = Mock(RegisterInstanceService)
        def handler = new ActuatorSagaHandler()
        handler.registerInstanceService = service
        when:
        handler.refreshAsgard('{"service": "test", "version": "1.0.0", "asgard": {}}')
        then:
        1 * service.propertyDataConsume(_, '1.0.0')
    }
}
