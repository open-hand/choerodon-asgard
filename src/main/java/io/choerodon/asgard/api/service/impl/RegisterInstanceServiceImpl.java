package io.choerodon.asgard.api.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.choerodon.asgard.api.dto.RegisterInstancePayloadDTO;
import io.choerodon.asgard.api.service.RegisterInstanceService;
import io.choerodon.asgard.api.service.SagaService;
import io.choerodon.asgard.api.service.SagaTaskInstanceService;
import io.choerodon.asgard.api.service.SagaTaskService;
import io.choerodon.asgard.infra.utils.ConvertUtils;
import io.choerodon.swagger.property.PropertyData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static io.choerodon.asgard.api.eventhandler.RegisterInstanceListener.REGISTER_TOPIC;

@Service
public class RegisterInstanceServiceImpl implements RegisterInstanceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterInstanceService.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private RestTemplate restTemplate = new RestTemplate();
    private Map<String, Integer> failTimeMap = new HashMap<>();
    @Value("${choerodon.asgard.fetch.time:20}")
    private Integer sagaFetchTime;
    @Value("${choerodon.asgard.isLocal:false}")
    private boolean isLocal;
    private KafkaTemplate<byte[], byte[]> kafkaTemplate;

    private SagaService sagaService;

    private SagaTaskService sagaTaskService;

    private SagaTaskInstanceService sagaTaskInstanceService;

    public RegisterInstanceServiceImpl(KafkaTemplate<byte[], byte[]> kafkaTemplate, SagaService sagaService,
                                       SagaTaskService sagaTaskService, SagaTaskInstanceService sagaTaskInstanceService) {
        this.kafkaTemplate = kafkaTemplate;
        this.sagaService = sagaService;
        this.sagaTaskService = sagaTaskService;
        this.sagaTaskInstanceService = sagaTaskInstanceService;
    }

    @Override
    public void msgConsumer(RegisterInstancePayloadDTO payload, boolean up) {
        if (up) {
            instanceUpConsumer(payload);
        } else {
            instanceDownConsumer(payload);
        }
    }

    private void instanceDownConsumer(final RegisterInstancePayloadDTO payload) {
        sagaTaskInstanceService.unlockByInstance(payload.getInstanceAddress());
    }

    private void instanceUpConsumer(final RegisterInstancePayloadDTO payload) {
        PropertyData propertyData = fetchPropertyData(payload.getInstanceAddress());
        if (propertyData == null) {
            Integer time = failTimeMap.get(payload.getInstanceAddress());
            if (time == null) {
                time = 0;
            }
            try {
                if (time < sagaFetchTime) {
                    kafkaTemplate.send(REGISTER_TOPIC, mapper.writeValueAsBytes(payload));
                    failTimeMap.put(payload.getInstanceAddress(), ++time);
                } else {
                    failTimeMap.remove(payload.getInstanceAddress());
                    LOGGER.warn("fetched property data failed too many times {}", payload);
                }

            } catch (JsonProcessingException e) {
                LOGGER.warn("error happened when instancePayload serialize {}", e.getMessage());
            }
        } else {
            propertyDataConsume(propertyData);
            failTimeMap.remove(payload.getInstanceAddress());
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
        try {
            propertyData.getSagas().stream()
                    .map(t -> ConvertUtils.convertSaga(t, propertyData.getService()))
                    .forEach(sagaService::createSaga);
            sagaTaskService.createSagaTaskList(propertyData.getSagaTasks().stream()
                    .map(t -> ConvertUtils.convertSagaTask(t, propertyData.getService()))
                    .collect(Collectors.toList()), propertyData.getService());
        } catch (Exception e) {
            LOGGER.warn("error.PropertyDataConsume {} propertyData {} ", e, propertyData);
        }
    }

}
