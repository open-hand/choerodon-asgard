package io.choerodon.asgard.infra.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import io.choerodon.asgard.api.dto.JsonMergeDTO;
import io.choerodon.asgard.domain.*;
import io.choerodon.asgard.infra.mapper.JsonDataMapper;
import io.choerodon.asgard.property.PropertyJobTask;
import io.choerodon.asgard.property.PropertySaga;
import io.choerodon.asgard.property.PropertySagaTask;
import io.choerodon.core.exception.CommonException;
import org.modelmapper.ModelMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConvertUtils {

    private ConvertUtils() {
    }

    public static QuartzMethod convertQuartzMethod(final ObjectMapper mapper, final PropertyJobTask jobTask, final String service) {
       final  QuartzMethod method = new QuartzMethod();
       method.setService(service);
       method.setMaxRetryCount(jobTask.getMaxRetryCount());
       method.setMethod(jobTask.getMethod());
        try {
            String params = mapper.writeValueAsString(jobTask.getParams());
            method.setParams(params);
            return method;
        } catch (JsonProcessingException e) {
            throw new CommonException("error.ConvertUtils.convertQuartzMethod", e);
        }
    }

    public static Saga convertSaga(final ModelMapper mapper, final PropertySaga saga, final String service) {
        Saga sagaDO = mapper.map(saga, Saga.class);
        sagaDO.setService(service);
        return sagaDO;
    }

    public static SagaTask convertSagaTask(final ModelMapper mapper, final PropertySagaTask sagaTask, final String service) {
        SagaTask sagaTaskDO = mapper.map(sagaTask, SagaTask.class);
        sagaTaskDO.setService(service);
        return sagaTaskDO;
    }


    public static List<JsonMergeDTO> convertToJsonMerge(final List<SagaTaskInstance> seqTaskInstances, final JsonDataMapper jsonDataMapper) {
        List<JsonMergeDTO> list = new ArrayList<>(seqTaskInstances.size());
        for (SagaTaskInstance sagaTaskInstance : seqTaskInstances) {
            if (sagaTaskInstance.getOutputDataId() == null) {
                continue;
            }
            JsonData jsonData = jsonDataMapper.selectByPrimaryKey(sagaTaskInstance.getOutputDataId());
            if (jsonData != null && jsonData.getData() != null) {
                list.add(new JsonMergeDTO(sagaTaskInstance.getTaskCode(), jsonData.getData()));
            }
        }
        return list;
    }

    public static String jsonMerge(final List<JsonMergeDTO> mergeDTOS, final ObjectMapper objectMapper) throws IOException {
        ObjectNode root = objectMapper.createObjectNode();
        if (mergeDTOS.isEmpty()) {
            return root.toString();
        }
        //元素都相同则直接返回任意一个
        if (isAllTheSame(mergeDTOS)) {
            return mergeDTOS.get(0).getTaskOutputJsonData();
        }
        for (JsonMergeDTO dto : mergeDTOS) {
            JsonNode jsonNode = objectMapper.readTree(dto.getTaskOutputJsonData());
            if (jsonNode instanceof ObjectNode) {
                root.setAll((ObjectNode) jsonNode);
            } else if (jsonNode instanceof ArrayNode) {
                root.putArray(dto.getTaskCode()).addAll((ArrayNode) jsonNode);
            } else if (jsonNode instanceof ValueNode) {
                root.set(dto.getTaskCode(), jsonNode);
            }
        }
        return root.toString();
    }

    /**
     * 判断所有元素是否相同
     */
    private static boolean isAllTheSame(final List<JsonMergeDTO> mergeDTOS) {
        if (mergeDTOS.size() == 1) {
            return true;
        }
        if (mergeDTOS.size() > 1) {
            for (int i = 0; i < mergeDTOS.size() - 1; i++) {
                if (!mergeDTOS.get(i).getTaskOutputJsonData().equals(mergeDTOS.get(i + 1).getTaskOutputJsonData())) {
                    return false;
                }
            }
        }
        return true;
    }

}
