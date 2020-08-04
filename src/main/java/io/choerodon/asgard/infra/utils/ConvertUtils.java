package io.choerodon.asgard.infra.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;

import io.choerodon.asgard.api.vo.JsonMerge;
import io.choerodon.asgard.infra.dto.*;
import io.choerodon.asgard.infra.mapper.JsonDataMapper;
import io.choerodon.asgard.property.PropertyJobTask;
import io.choerodon.asgard.property.PropertySaga;
import io.choerodon.asgard.property.PropertySagaTask;
import io.choerodon.asgard.property.PropertyTimedTask;
import io.choerodon.asgard.schedule.QuartzDefinition;
import io.choerodon.asgard.schedule.enums.TriggerTypeEnum;
import io.choerodon.core.exception.CommonException;

import org.modelmapper.ModelMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ConvertUtils {

    private ConvertUtils() {
    }

    public static QuartzMethodDTO convertQuartzMethod(final ObjectMapper mapper, final PropertyJobTask jobTask, final String service) {
        final QuartzMethodDTO method = new QuartzMethodDTO();
        method.setService(service);
        method.setCode(jobTask.getCode());
        method.setDescription(jobTask.getDescription());
        method.setMaxRetryCount(jobTask.getMaxRetryCount());
        method.setMethod(jobTask.getMethod());
        method.setLevel(jobTask.getLevel());
        try {
            String params = mapper.writeValueAsString(jobTask.getParams());
            method.setParams(params);
            return method;
        } catch (JsonProcessingException e) {
            throw new CommonException("error.ConvertUtils.convertQuartzMethod", e);
        }
    }

    public static QuartzTaskDTO convertQuartzTask(final ObjectMapper mapper, final PropertyTimedTask timedTask) {
        final QuartzTaskDTO task = new QuartzTaskDTO();
        task.setId(null);
        task.setName(timedTask.getName());
        task.setDescription(timedTask.getDescription());
        task.setExecuteMethod(timedTask.getMethodCode());
        task.setTriggerType(timedTask.getTriggerType());
        task.setStartTime(new Date());
        task.setCronExpression(timedTask.getCronExpression());
        task.setSimpleRepeatCount(timedTask.getRepeatCount());
        task.setSimpleRepeatInterval(timedTask.getRepeatInterval());
        task.setSimpleRepeatIntervalUnit(timedTask.getRepeatIntervalUnit());
        task.setStatus(QuartzDefinition.TaskStatus.ENABLE.name());
        if (task.getTriggerType().equals(TriggerTypeEnum.simple_trigger.getType())) {
            if (timedTask.getOneExecution()) {
                task.setCronExpression("1");
            } else {
                task.setCronExpression("0");
            }
        }
        try {
            String params = mapper.writeValueAsString(timedTask.getParams());
            task.setExecuteParams(params);
            return task;
        } catch (JsonProcessingException e) {
            throw new CommonException("error.ConvertUtils.convertQuartzTask", e);
        }
    }

    public static SagaDTO convertSaga(final ModelMapper mapper, final PropertySaga saga, final String service) {
        SagaDTO sagaDO = mapper.map(saga, SagaDTO.class);
        sagaDO.setService(service);
        return sagaDO;
    }

    public static SagaTaskDTO convertSagaTask(final ModelMapper mapper, final PropertySagaTask sagaTask, final String service) {
        SagaTaskDTO sagaTaskDO = mapper.map(sagaTask, SagaTaskDTO.class);
        sagaTaskDO.setService(service);
        return sagaTaskDO;
    }


    public static List<JsonMerge> convertToJsonMerge(final List<SagaTaskInstanceDTO> seqTaskInstances, final JsonDataMapper jsonDataMapper) {
        List<JsonMerge> list = new ArrayList<>(seqTaskInstances.size());
        for (SagaTaskInstanceDTO sagaTaskInstance : seqTaskInstances) {
            if (sagaTaskInstance.getOutputDataId() == null) {
                continue;
            }
            JsonDataDTO jsonData = jsonDataMapper.selectByPrimaryKey(sagaTaskInstance.getOutputDataId());
            if (jsonData != null && jsonData.getData() != null) {
                list.add(new JsonMerge(sagaTaskInstance.getTaskCode(), jsonData.getData()));
            }
        }
        return list;
    }

    public static String jsonMerge(final List<JsonMerge> mergeDTOS, final ObjectMapper objectMapper) throws IOException {
        ObjectNode root = objectMapper.createObjectNode();
        if (mergeDTOS.isEmpty()) {
            return root.toString();
        }
        //元素都相同则直接返回任意一个
        if (isAllTheSame(mergeDTOS)) {
            return mergeDTOS.get(0).getTaskOutputJsonData();
        }
        for (JsonMerge dto : mergeDTOS) {
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
    private static boolean isAllTheSame(final List<JsonMerge> mergeDTOS) {
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
