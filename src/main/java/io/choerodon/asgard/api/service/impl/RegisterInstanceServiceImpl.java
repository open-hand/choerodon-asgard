package io.choerodon.asgard.api.service.impl;

import io.choerodon.asgard.api.dto.RegisterInstancePayloadDTO;
import io.choerodon.asgard.api.service.RegisterInstanceService;
import io.choerodon.asgard.api.service.SagaService;
import io.choerodon.asgard.api.service.SagaTaskInstanceService;
import io.choerodon.asgard.api.service.SagaTaskService;
import io.choerodon.asgard.infra.utils.ConvertUtils;
import io.choerodon.asgard.saga.property.PropertyData;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.stream.Collectors;

@Service
public class RegisterInstanceServiceImpl implements RegisterInstanceService {

    private RestTemplate restTemplate = new RestTemplate();

    @Value("${choerodon.asgard.isLocal:false}")
    private Boolean isLocal;

    private SagaService sagaService;

    private SagaTaskService sagaTaskService;

    private SagaTaskInstanceService sagaTaskInstanceService;

    private final ModelMapper modelMapper = new ModelMapper();

    public RegisterInstanceServiceImpl(SagaService sagaService,
                                       SagaTaskService sagaTaskService,
                                       SagaTaskInstanceService sagaTaskInstanceService) {
        this.sagaService = sagaService;
        this.sagaTaskService = sagaTaskService;
        this.sagaTaskInstanceService = sagaTaskInstanceService;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
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
        if (isLocal) {
            address = "127.0.0.1:" + address.split(":")[1];
        }
        ResponseEntity<PropertyData> response = restTemplate.getForEntity("http://"
                + address + "/choerodon/asgard", PropertyData.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new RemoteAccessException("error.fetchPropertyData");
        }
    }


    private void propertyDataConsume(final PropertyData propertyData) {
        propertyData.getSagas().stream()
                .map(t -> ConvertUtils.convertSaga(modelMapper, t, propertyData.getService()))
                .forEach(sagaService::create);
        sagaTaskService.createSagaTaskList(propertyData.getSagaTasks().stream()
                .map(t -> ConvertUtils.convertSagaTask(modelMapper, t, propertyData.getService()))
                .collect(Collectors.toList()), propertyData.getService());
    }

}
