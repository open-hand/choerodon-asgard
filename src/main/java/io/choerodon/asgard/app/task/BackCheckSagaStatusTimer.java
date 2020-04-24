package io.choerodon.asgard.app.task;

import feign.Client;
import feign.hystrix.HystrixFeign;
import io.choerodon.asgard.app.service.SagaInstanceService;
import io.choerodon.asgard.infra.config.AsgardProperties;
import io.choerodon.asgard.infra.dto.SagaInstanceDTO;
import io.choerodon.asgard.infra.feign.StatusQueryFeign;
import io.choerodon.asgard.infra.mapper.SagaInstanceMapper;
import io.choerodon.asgard.infra.utils.JsonDecoder;
import io.choerodon.asgard.saga.dto.SagaStatusQueryDTO;
import io.choerodon.feign.FeignRequestInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 回查未被确认的saga实例的状态
 */
public class BackCheckSagaStatusTimer {

    private static final Logger LOGGER = LoggerFactory.getLogger(BackCheckSagaStatusTimer.class);

    private ScheduledExecutorService scheduledExecutorService;

    private SagaInstanceMapper instanceMapper;

    private AsgardProperties asgardProperties;

    private FeignRequestInterceptor feignRequestInterceptor;

    private Client client;

    private SagaInstanceService sagaInstanceService;

    private final JsonDecoder jsonDecoder = new JsonDecoder();

    public BackCheckSagaStatusTimer(@Qualifier("backCheckSagaStatusTimer") ScheduledExecutorService service,
                                    SagaInstanceMapper instanceMapper,
                                    AsgardProperties asgardProperties,
                                    FeignRequestInterceptor feignRequestInterceptor,
                                    Client client,
                                    SagaInstanceService sagaInstanceService) {
        this.scheduledExecutorService = service;
        this.instanceMapper = instanceMapper;
        this.asgardProperties = asgardProperties;
        this.feignRequestInterceptor = feignRequestInterceptor;
        this.client = client;
        this.sagaInstanceService = sagaInstanceService;
    }

    @PostConstruct
    public void backCheck() {
        scheduledExecutorService.scheduleWithFixedDelay(() ->
                        instanceMapper.selectUnConfirmedTimeOutInstance(asgardProperties.getSaga().getUnConfirmedTimeoutSeconds(), new Date())
                                .forEach(this::query)
                , 20000, asgardProperties.getSaga().getBackCheckIntervalMs(), TimeUnit.MILLISECONDS);
    }

    private void query(final SagaInstanceDTO sagaInstance) {
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
        SagaStatusQueryDTO status = query.getEventRecord(sagaInstance.getUuid());
        if (SagaStatusQueryDTO.STATUS_CANCEL.equals(status.getStatus())) {
            sagaInstanceService.cancel(sagaInstance.getUuid());
        } else if (SagaStatusQueryDTO.STATUS_CONFIRM.equals(status.getStatus())) {
            sagaInstanceService.confirm(sagaInstance.getUuid(), status.getPayload(), status.getRefType(), status.getRefId());
        }
    }

}
