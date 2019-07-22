package io.choerodon.asgard.app.task;

import io.choerodon.asgard.infra.dto.SagaDTO;
import io.choerodon.asgard.infra.dto.SagaTaskDTO;
import io.choerodon.asgard.infra.mapper.SagaMapper;
import io.choerodon.asgard.infra.mapper.SagaTaskMapper;
import io.choerodon.asgard.schedule.QuartzDefinition;
import io.choerodon.asgard.schedule.annotation.JobTask;
import io.choerodon.asgard.schedule.annotation.TimedTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.Arrays;
import java.util.Map;

/**
 * 合并或移除的服务，配置对应的服务名，移除saga和sagaTask
 *
 * @author superlee
 * @since 2019-07-21
 */
@Component
public class RemoveDeprecatedSaga {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoveDeprecatedSaga.class);

    private final String[] deprecatedService;

    private final SagaMapper sagaMapper;

    private final SagaTaskMapper sagaTaskMapper;

    public RemoveDeprecatedSaga(@Value("${choerodon.saga.deprecated:#{null}}") final String[] deprecatedService,
                                SagaMapper sagaMapper,
                                SagaTaskMapper sagaTaskMapper) {
        this.sagaMapper = sagaMapper;
        this.sagaTaskMapper = sagaTaskMapper;
        this.deprecatedService = deprecatedService;
    }


    @TimedTask(name = "移除指定服务的saga和sagaTask", description = "移除指定服务的saga和sagaTask", oneExecution = true, repeatCount = 0,
            repeatIntervalUnit = QuartzDefinition.SimpleRepeatIntervalUnit.SECONDS, repeatInterval = 100, params = {})
    @JobTask(code = "removeDeprecatedSaga", description = "移除指定服务的saga和sagaTask")
    public void remove(Map<String, Object> data) {
        if (ObjectUtils.isEmpty(deprecatedService)) {
            return;
        }
        String services = Arrays.toString(deprecatedService);
        LOGGER.info("remove deprecated saga and sagaTask, services: {}", services);
        for (String service : deprecatedService) {
            SagaDTO sagaDTO = new SagaDTO();
            sagaDTO.setService(service);
            sagaMapper.delete(sagaDTO);
            SagaTaskDTO sagaTaskDTO = new SagaTaskDTO();
            sagaTaskDTO.setService(service);
            sagaTaskMapper.delete(sagaTaskDTO);
        }
    }
}
