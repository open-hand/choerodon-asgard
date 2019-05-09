package io.choerodon.asgard.api.service.impl;

import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.choerodon.eureka.event.EurekaEventPayload;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import io.choerodon.asgard.api.service.*;
import io.choerodon.asgard.infra.utils.ConvertUtils;
import io.choerodon.asgard.property.PropertyData;

@Service
public class RegisterInstanceServiceImpl implements RegisterInstanceService {

    private RestTemplate restTemplate = new RestTemplate();

    private final SagaService sagaService;

    private final SagaTaskService sagaTaskService;

    private final SagaTaskInstanceService sagaTaskInstanceService;

    private final ScheduleTaskInstanceService scheduleTaskInstanceService;

    private final ModelMapper modelMapper = new ModelMapper();

    private final ObjectMapper objectMapper = new ObjectMapper();

    private QuartzMethodService quartzMethodService;

    private ScheduleTaskService scheduleTaskService;

    public RegisterInstanceServiceImpl(SagaService sagaService,
                                       SagaTaskService sagaTaskService,
                                       SagaTaskInstanceService sagaTaskInstanceService,
                                       QuartzMethodService quartzMethodService,
                                       ScheduleTaskService scheduleTaskService,
                                       ScheduleTaskInstanceService scheduleTaskInstanceService) {
        this.sagaService = sagaService;
        this.sagaTaskService = sagaTaskService;
        this.sagaTaskInstanceService = sagaTaskInstanceService;
        this.quartzMethodService = quartzMethodService;
        this.scheduleTaskService = scheduleTaskService;
        this.scheduleTaskInstanceService = scheduleTaskInstanceService;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void instanceDownConsumer(final EurekaEventPayload payload) {
        sagaTaskInstanceService.unlockByInstance(payload.getInstanceAddress());
        scheduleTaskInstanceService.unlockByInstance(payload.getInstanceAddress());
    }

    @Override
    public void instanceUpConsumer(final EurekaEventPayload payload) {
        PropertyData propertyData = fetchPropertyData(payload.getInstanceAddress());
        if (propertyData == null) {
            throw new RemoteAccessException("error.instanceUpConsumer.fetchPropertyData");
        } else {
            propertyDataConsume(propertyData, payload.getVersion());
        }
    }

    private PropertyData fetchPropertyData(String address) {
        ResponseEntity<PropertyData> response = restTemplate.getForEntity("http://"
                + address + "/choerodon/asgard", PropertyData.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new RemoteAccessException("error.fetchPropertyData.statusCodeNot2XX");
        }
    }


    @Override
    public void propertyDataConsume(final PropertyData propertyData, final String version) {
        propertyData.getSagas().stream()
                .map(t -> ConvertUtils.convertSaga(modelMapper, t, propertyData.getService()))
                .forEach(sagaService::create);
        sagaTaskService.createSagaTaskList(propertyData.getSagaTasks().stream()
                .map(t -> ConvertUtils.convertSagaTask(modelMapper, t, propertyData.getService()))
                .collect(Collectors.toList()), propertyData.getService());
        quartzMethodService.createMethodList(propertyData.getService(), propertyData.getJobTasks().stream()
                .map(t -> ConvertUtils.convertQuartzMethod(objectMapper, t, propertyData.getService())).collect(Collectors.toList()));
        scheduleTaskService.createTaskList(propertyData.getService(), propertyData.getTimedTasks(), version);
    }

}
