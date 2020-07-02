package io.choerodon.asgard.app.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.choerodon.asgard.app.service.*;
import io.choerodon.asgard.infra.utils.ConvertUtils;
import io.choerodon.asgard.property.PropertyData;
import org.hzero.register.event.event.InstanceAddedEvent;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RegisterInstanceServiceImpl implements RegisterInstanceService {

    private final Logger LOGGER = LoggerFactory.getLogger(RegisterInstanceServiceImpl.class);
    private static final String VERSION = "VERSION";

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
    public void instanceDownConsumer(final InstanceAddedEvent payload) {
        sagaTaskInstanceService.unlockByInstance(payload.getServiceInstance().getHost());
        scheduleTaskInstanceService.unlockByInstance(payload.getServiceInstance().getHost());
    }

    /**
     * @param payload
     */
    @Override
    public void instanceUpConsumer(final InstanceAddedEvent payload) {

        if (payload.getServiceInstance() == null) {
            LOGGER.error("[InstanceAddedEvent=" + payload + "] has no ServiceInstance");
        } else {
            updateConsumer(payload.getServiceInstance());
        }


//        PropertyData propertyData = fetchPropertyData(payload.getServiceInstance().getHost() + ":" + payload.getServiceInstance().getPort());
//        if (propertyData == null) {
//            throw new RemoteAccessException("error.instanceUpConsumer.fetchPropertyData");
//        } else {
//            propertyDataConsume(propertyData, payload.getServiceInstance().getMetadata().get("VERSION"));
//        }
    }

    @Override
    public void updateConsumer(ServiceInstance instance) {
        String address = instance.getHost() + ":" + instance.getPort();
        Map<String, String> metadata = instance.getMetadata();
        PropertyData propertyData = fetchPropertyData(address);
        if (propertyData == null) {
            throw new RemoteAccessException("error.instanceUpConsumer.fetchPropertyData");
        } else {
            propertyDataConsume(propertyData, metadata.get(VERSION));
        }
    }

    /**
     * @param address
     * @return
     */
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
