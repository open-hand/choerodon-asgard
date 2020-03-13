package io.choerodon.asgard.app.eventhandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.choerodon.asgard.app.service.RegisterInstanceService;
import io.choerodon.asgard.property.PropertyData;
import io.choerodon.asgard.saga.annotation.SagaTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ActuatorSagaHandler {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String ACTUATOR_REFRESH_SAGA_CODE = "mgmt-actuator-refresh";
    private static final String SAGA_REFRESH_SAGA_CODE = "asgard-saga-refresh";

    @Autowired
    private RegisterInstanceService registerInstanceService;

    @SagaTask(code = SAGA_REFRESH_SAGA_CODE, sagaCode = ACTUATOR_REFRESH_SAGA_CODE, seq = 1, description = "刷新Asgard数据")
    public String refreshAsgard(String actuatorJson) throws IOException {
        JsonNode actuator = OBJECT_MAPPER.readTree(actuatorJson);
        String version = actuator.get("version").asText();
        JsonNode metadataNode = actuator.get("asgard");
        if (metadataNode == null || metadataNode.isNull()){
            return actuatorJson;
        }
        PropertyData propertyData = OBJECT_MAPPER.readValue(metadataNode.toString(), PropertyData.class);
        registerInstanceService.propertyDataConsume(propertyData, version);
        return actuatorJson;
    }
}
