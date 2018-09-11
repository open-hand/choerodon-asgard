package io.choerodon.asgard.api.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.choerodon.asgard.api.dto.ScheduleMethodDTO;
import io.choerodon.asgard.api.service.ScheduleMethodService;
import io.choerodon.asgard.infra.mapper.QuartzMethodMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduleMethodServiceImpl implements ScheduleMethodService {

    private QuartzMethodMapper methodMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ScheduleMethodServiceImpl(QuartzMethodMapper methodMapper) {
        this.methodMapper = methodMapper;
    }

    @Override
    public List<ScheduleMethodDTO> list() {
        return methodMapper.selectAll().stream().map(t -> new ScheduleMethodDTO(t, objectMapper)).collect(Collectors.toList());
    }

}
