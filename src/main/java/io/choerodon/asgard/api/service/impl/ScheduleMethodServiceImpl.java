package io.choerodon.asgard.api.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import io.choerodon.asgard.api.dto.ScheduleMethodDTO;
import io.choerodon.asgard.api.dto.ScheduleMethodInfoDTO;
import io.choerodon.asgard.api.dto.ScheduleMethodParamsDTO;
import io.choerodon.asgard.api.service.ScheduleMethodService;
import io.choerodon.asgard.domain.QuartzMethod;
import io.choerodon.asgard.infra.enums.DefaultAutowiredField;
import io.choerodon.asgard.infra.mapper.QuartzMethodMapper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

@Service
public class ScheduleMethodServiceImpl implements ScheduleMethodService {

    public static final String DEFAULT = "default";
    public static final String FIELD_NAME = "name";
    private QuartzMethodMapper methodMapper;

    @Autowired
    DiscoveryClient discoveryClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ScheduleMethodServiceImpl(QuartzMethodMapper methodMapper) {
        this.methodMapper = methodMapper;
    }

    public void setDiscoveryClient(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    public void setMethodMapper(QuartzMethodMapper methodMapper) {
        this.methodMapper = methodMapper;
    }

    @Override
    public ResponseEntity<Page<ScheduleMethodInfoDTO>> pageQuery(PageRequest pageRequest, String code, String service, String method, String description, String params, String level) {
        Page<QuartzMethod> page = PageHelper.doPageAndSort(pageRequest,
                () -> methodMapper.fulltextSearch(code, service, method, description, params, level));
        Page<ScheduleMethodInfoDTO> pageBack = pageConvert(page);
        return new ResponseEntity<>(pageBack, HttpStatus.OK);
    }

    public Page<ScheduleMethodInfoDTO> pageConvert(Page<QuartzMethod> page) {
        List<ScheduleMethodInfoDTO> scheduleMethodInfoDTOS = new ArrayList<>();
        Page<ScheduleMethodInfoDTO> pageBack = new Page<>();
        pageBack.setNumber(page.getNumber());
        pageBack.setNumberOfElements(page.getNumberOfElements());
        pageBack.setSize(page.getSize());
        pageBack.setTotalElements(page.getTotalElements());
        pageBack.setTotalPages(page.getTotalPages());
        if (page.getContent().isEmpty()) {
            return pageBack;
        } else {
            page.getContent().forEach(t ->
                    scheduleMethodInfoDTOS.add(new ScheduleMethodInfoDTO(t.getId(), t.getCode(), t.getService(), t.getMethod(), t.getDescription(), discoveryClient.getInstances(t.getService()).size())));
            pageBack.setContent(scheduleMethodInfoDTOS);
            return pageBack;
        }
    }

    @Override
    public List<ScheduleMethodDTO> getMethodByService(String serviceName, String level) {
        return methodMapper.selectByService(serviceName, level).stream().map(t -> new ScheduleMethodDTO(t, objectMapper)).collect(Collectors.toList());
    }

    @Override
    public ScheduleMethodParamsDTO getParams(Long id, String level) {
        QuartzMethod method = methodMapper.selectByPrimaryKey(id);
        if (method == null) {
            throw new CommonException("error.scheduleMethod.notExist");
        }
        if (!level.equals(method.getLevel())) {
            throw new CommonException("error.scheduleMethod.levelNotMatch");
        }
        ScheduleMethodParamsDTO scheduleMethodParamsDTO = methodMapper.selectParamsById(id);
        return markDefaultField(new ScheduleMethodParamsDTO(scheduleMethodParamsDTO.getId(), scheduleMethodParamsDTO.getParamsJson(), objectMapper)
                , method.getLevel());
    }

    /**
     * 给方法中标记是否是默认字段
     * organization层默认字段：organizationId,organizationCode,organizationName
     * project层默认字段：projectId,projectCode,projectName
     *
     * @param scheduleMethodParamsDTO
     * @param level
     * @return
     */
    private ScheduleMethodParamsDTO markDefaultField(ScheduleMethodParamsDTO scheduleMethodParamsDTO, final String level) {
        List<Map<String, Object>> maps = scheduleMethodParamsDTO.getParamsList();
        if (ResourceLevel.ORGANIZATION.value().equals(level)) {
            maps.forEach(map -> {
                if (Arrays.asList(DefaultAutowiredField.organizationDefaultField()).contains(map.get(FIELD_NAME).toString())) {
                    map.put(DEFAULT, true);
                } else {
                    map.put(DEFAULT, false);
                }
            });
        } else if (ResourceLevel.PROJECT.value().equals(level)) {
            maps.forEach(map -> {
                if (Arrays.asList(DefaultAutowiredField.projectDefaultField()).contains(map.get(FIELD_NAME).toString())) {
                    map.put(DEFAULT, true);
                } else {
                    map.put(DEFAULT, false);
                }
            });
        } else if (ResourceLevel.SITE.value().equals(level)) {
            maps.forEach(map -> map.put(DEFAULT, false));
        }
        scheduleMethodParamsDTO.setParamsList(maps);
        try {
            scheduleMethodParamsDTO.setParamsJson(objectMapper.writeValueAsString(maps));
        } catch (JsonProcessingException e) {
            throw new CommonException("error.ScheduleMethodParamsDTO.jsonIOException", e);
        }
        return scheduleMethodParamsDTO;
    }

    @Override
    public Long getMethodIdByCode(String code) {
        QuartzMethod t = new QuartzMethod();
        t.setCode(code);
        QuartzMethod method = methodMapper.selectOne(t);
        if (method == null) {
            throw new CommonException("error.scheduleMethod.notExist");
        }
        return method.getId();
    }

    @Override
    public List<String> getServices(String level) {
        QuartzMethod method = new QuartzMethod();
        method.setLevel(level);
        List<String> list = new ArrayList<>();
        methodMapper.select(method).forEach(m -> list.add(m.getService()));
        return list.stream().distinct().collect(Collectors.toList());
    }
}
