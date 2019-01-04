package io.choerodon.asgard.api.service.impl;

import feign.Client;
import feign.hystrix.HystrixFeign;
import io.choerodon.asgard.api.service.SagaInstanceService;
import io.choerodon.asgard.config.AsgardProperties;
import io.choerodon.asgard.domain.SagaInstance;
import io.choerodon.asgard.infra.feign.StatusQueryFeign;
import io.choerodon.asgard.infra.mapper.SagaInstanceMapper;
import io.choerodon.asgard.infra.utils.JsonDecoder;
import io.choerodon.asgard.saga.producer.ProducerBackCheckEndpoint;
import io.choerodon.feign.FeignRequestInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class SagaScheduledBackCheckStatusImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(SagaScheduledBackCheckStatusImpl.class);

    private ScheduledExecutorService scheduledExecutorService;

    private SagaInstanceMapper instanceMapper;

    private AsgardProperties asgardProperties;

    private FeignRequestInterceptor feignRequestInterceptor;

    private Client client;

    private JsonDecoder jsonDecoder;

    private SagaInstanceService sagaInstanceService;

    public SagaScheduledBackCheckStatusImpl(@Qualifier("sagaBackCheckScheduledService") ScheduledExecutorService service,
                                            SagaInstanceMapper instanceMapper,
                                            AsgardProperties asgardProperties,
                                            FeignRequestInterceptor feignRequestInterceptor,
                                            Client client,
                                            JsonDecoder jsonDecoder,
                                            SagaInstanceService sagaInstanceService) {
        this.scheduledExecutorService = service;
        this.instanceMapper = instanceMapper;
        this.asgardProperties = asgardProperties;
        this.feignRequestInterceptor = feignRequestInterceptor;
        this.client = client;
        this.jsonDecoder = jsonDecoder;
        this.sagaInstanceService = sagaInstanceService;
    }

    @PostConstruct
    public void backCheck() {
        scheduledExecutorService.scheduleWithFixedDelay(() ->
                        instanceMapper.selectUnConfirmedTimeOutInstance(asgardProperties.getSaga().getUnConfirmedTimeoutSeconds())
                                .forEach(this::query)
                , 20000, asgardProperties.getSaga().getBackCheckIntervalMs(), TimeUnit.MILLISECONDS);
    }

    private void query(final SagaInstance sagaInstance) {
        if (sagaInstance.getCreatedOn() == null || sagaInstance.getUuid() == null) {
            LOGGER.warn("error.sagaScheduledBackCheckStatus.sagaInstanceInvalid, sagaInstance: {}", sagaInstance);
            return;
        }
        StatusQueryFeign query = HystrixFeign
                .builder()
                .client(client)
                .requestInterceptor(feignRequestInterceptor)
                .decoder(jsonDecoder)
                .target(StatusQueryFeign.class, "http://" + sagaInstance.getCreatedOn());
        String status = query.getEventRecord(sagaInstance.getUuid());
        if (ProducerBackCheckEndpoint.STATUS_CANCEL.equals(status)) {
           sagaInstanceService.cancel(sagaInstance.getUuid());
        } else if (ProducerBackCheckEndpoint.STATUS_CONFIRM.equals(status)) {
            sagaInstanceService.confirm(sagaInstance.getUuid(), "{}");
        }
    }

}
