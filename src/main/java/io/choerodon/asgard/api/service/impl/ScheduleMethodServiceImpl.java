package io.choerodon.asgard.api.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
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
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;

@Service
public class ScheduleMethodServiceImpl implements ScheduleMethodService {

    public static final String DEFAULT = "default";
    public static final String FIELD_NAME = "name";

    private static final String SCHEDULE_METHOD_NOT_EXIST_EXCEPTION = "error.scheduleMethod.notExist";
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
    public ResponseEntity<PageInfo<ScheduleMethodInfoDTO>> pageQuery(int page, int size, String code, String service, String method, String description, String params, String level) {
        PageInfo<QuartzMethod> pageInfo =
                PageHelper
                        .startPage(page, size)
                        .doSelectPageInfo(
                                () -> methodMapper.fulltextSearch(code, service, method, description, params, level));
        List<QuartzMethod> result = pageInfo.getList();
        List<ScheduleMethodInfoDTO> scheduleMethodInfos = new ArrayList<>();
        result.forEach(r ->
                scheduleMethodInfos.add(
                        new ScheduleMethodInfoDTO(r.getId(), r.getCode(),
                                r.getService(), r.getMethod(), r.getDescription(),
                                discoveryClient.getInstances(r.getService()).size(), r.getLevel())));
        Page<ScheduleMethodInfoDTO> pageResult = new Page<>(page, size);
        pageResult.setTotal(pageInfo.getTotal());
        pageResult.addAll(scheduleMethodInfos);
        return new ResponseEntity<>(pageResult.toPageInfo(), HttpStatus.OK);
    }

    @Override
    public List<ScheduleMethodDTO> getMethodByService(String serviceName, String level) {
        return methodMapper.selectByService(serviceName, level).stream().map(t -> new ScheduleMethodDTO(t, objectMapper)).collect(Collectors.toList());
    }

    @Override
    public ScheduleMethodParamsDTO getParams(Long id, String level) {
        QuartzMethod method = methodMapper.selectByPrimaryKey(id);
        if (method == null) {
            throw new CommonException(SCHEDULE_METHOD_NOT_EXIST_EXCEPTION);
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
                boolean contains = Arrays.asList(DefaultAutowiredField.organizationDefaultField()).contains(map.get(FIELD_NAME).toString());
                map.put(DEFAULT, contains);
            });
        } else if (ResourceLevel.PROJECT.value().equals(level)) {
            maps.forEach(map -> {
                boolean contains = Arrays.asList(DefaultAutowiredField.projectDefaultField()).contains(map.get(FIELD_NAME).toString());
                map.put(DEFAULT, contains);
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
            throw new CommonException(SCHEDULE_METHOD_NOT_EXIST_EXCEPTION);
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

    @Override
    public void delete(Long id) {
        if (methodMapper.selectByPrimaryKey(id) == null) {
            throw new CommonException(SCHEDULE_METHOD_NOT_EXIST_EXCEPTION);
        }
        methodMapper.deleteByPrimaryKey(id);
    }
}
