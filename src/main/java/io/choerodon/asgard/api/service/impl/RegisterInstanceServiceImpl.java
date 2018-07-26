package io.choerodon.asgard.api.service.impl;

import io.choerodon.asgard.api.dto.RegisterInstancePayloadDTO;
import io.choerodon.asgard.api.service.RegisterInstanceService;
import io.choerodon.asgard.api.service.SagaService;
import io.choerodon.asgard.api.service.SagaTaskInstanceService;
import io.choerodon.asgard.api.service.SagaTaskService;
import io.choerodon.asgard.infra.utils.ConvertUtils;
import io.choerodon.swagger.property.PropertyData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.stream.Collectors;

@Service
public class RegisterInstanceServiceImpl implements RegisterInstanceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterInstanceService.class);

    private RestTemplate restTemplate = new RestTemplate();

    private boolean isLocal;

    private SagaService sagaService;

    private SagaTaskService sagaTaskService;

    private SagaTaskInstanceService sagaTaskInstanceService;

    public RegisterInstanceServiceImpl(SagaService sagaService,
                                       SagaTaskService sagaTaskService, SagaTaskInstanceService sagaTaskInstanceService) {
        this.sagaService = sagaService;
        this.sagaTaskService = sagaTaskService;
        this.sagaTaskInstanceService = sagaTaskInstanceService;
    }

    @Override
    public void instanceDownConsumer(final RegisterInstancePayloadDTO payload) {
        sagaTaskInstanceService.unlockByInstance(payload.getInstanceAddress());
    }

    @Override
    public void instanceUpConsumer(final RegisterInstancePayloadDTO payload) {
        PropertyData propertyData = fetchPropertyData(payload.getInstanceAddress());
        if (propertyData == null) {
            throw new RemoteAccessException("error.instanceUpConsumer.fetchPropertyData");
        } else {
            propertyDataConsume(propertyData);
        }
    }

    private PropertyData fetchPropertyData(String address) {
        try {
            if (isLocal) {
                address = "127.0.0.1:" + address.split(":")[1];
            }
            ResponseEntity<PropertyData> response = restTemplate.getForEntity("http://"
                    + address + "/choerodon/properties", PropertyData.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                LOGGER.info("error.fetchPropertyData {}", address);
            }
        } catch (RestClientException e) {
            LOGGER.info("error.fetchPropertyData {}", address);
        }
        return null;
    }


    private void propertyDataConsume(final PropertyData propertyData) {
        propertyData.getSagas().stream()
                .map(t -> ConvertUtils.convertSaga(t, propertyData.getService()))
                .forEach(sagaService::createSaga);
        sagaTaskService.createSagaTaskList(propertyData.getSagaTasks().stream()
                .map(t -> ConvertUtils.convertSagaTask(t, propertyData.getService()))
                .collect(Collectors.toList()), propertyData.getService());
    }

}
