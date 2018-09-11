package io.choerodon.asgard.api.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.choerodon.asgard.api.dto.RegisterInstancePayloadDTO;
import io.choerodon.asgard.api.service.*;
import io.choerodon.asgard.infra.utils.ConvertUtils;
import io.choerodon.asgard.property.PropertyData;
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

    private final SagaService sagaService;

    private final SagaTaskService sagaTaskService;

    private final SagaTaskInstanceService sagaTaskInstanceService;

    private final ScheduleTaskInstanceService scheduleTaskInstanceService;

    private final ModelMapper modelMapper = new ModelMapper();

    private final ObjectMapper objectMapper = new ObjectMapper();

    private QuartzMethodService quartzMethodService;

    public RegisterInstanceServiceImpl(SagaService sagaService,
                                       SagaTaskService sagaTaskService,
                                       SagaTaskInstanceService sagaTaskInstanceService,
                                       QuartzMethodService quartzMethodService,
                                       ScheduleTaskInstanceService scheduleTaskInstanceService) {
        this.sagaService = sagaService;
        this.sagaTaskService = sagaTaskService;
        this.sagaTaskInstanceService = sagaTaskInstanceService;
        this.quartzMethodService = quartzMethodService;
        this.scheduleTaskInstanceService = scheduleTaskInstanceService;
    }

    public void setLocal(Boolean local) {
        isLocal = local;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void instanceDownConsumer(final RegisterInstancePayloadDTO payload) {
        sagaTaskInstanceService.unlockByInstance(payload.getInstanceAddress());
        scheduleTaskInstanceService.unlockByInstance(payload.getInstanceAddress());
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
            throw new RemoteAccessException("error.fetchPropertyData.statusCodeNot2XX");
        }
    }


    private void propertyDataConsume(final PropertyData propertyData) {
        propertyData.getSagas().stream()
                .map(t -> ConvertUtils.convertSaga(modelMapper, t, propertyData.getService()))
                .forEach(sagaService::create);
        sagaTaskService.createSagaTaskList(propertyData.getSagaTasks().stream()
                .map(t -> ConvertUtils.convertSagaTask(modelMapper, t, propertyData.getService()))
                .collect(Collectors.toList()), propertyData.getService());
        quartzMethodService.createMethodList(propertyData.getService(), propertyData.getJobTasks().stream()
                .map(t -> ConvertUtils.convertQuartzMethod(objectMapper, t, propertyData.getService())).collect(Collectors.toList()));

    }

}
