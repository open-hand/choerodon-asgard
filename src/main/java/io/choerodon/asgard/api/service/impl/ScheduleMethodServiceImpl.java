package io.choerodon.asgard.api.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
import io.choerodon.asgard.infra.mapper.QuartzMethodMapper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

@Service
public class ScheduleMethodServiceImpl implements ScheduleMethodService {

    private QuartzMethodMapper methodMapper;

    @Autowired
    DiscoveryClient discoveryClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ScheduleMethodServiceImpl(QuartzMethodMapper methodMapper) {
        this.methodMapper = methodMapper;
    }

    @Override
    public List<ScheduleMethodDTO> list() {
        return methodMapper.selectAll().stream().map(t -> new ScheduleMethodDTO(t, objectMapper)).collect(Collectors.toList());
    }

    @Override
    public ResponseEntity<Page<ScheduleMethodInfoDTO>> pageQuery(PageRequest pageRequest, String code, String service, String method, String description, String params) {
        Page<QuartzMethod> page = PageHelper.doPageAndSort(pageRequest,
                () -> methodMapper.fulltextSearch(code, service, method, description, params));
        Page<ScheduleMethodInfoDTO> pageBack = pageConvert(page);
        return new ResponseEntity<>(pageBack, HttpStatus.OK);
//
//        return new ResponseEntity<>(PageHelper.doPageAndSort(
//                pageRequest,
//                () -> methodMapper.fulltextSearch(code, service, method, description, params)
//                        .stream()
//                        .map(t -> new ScheduleMethodInfoDTO(t.getId(), t.getCode(), t.getService(), t.getMethod(), t.getDescription(), discoveryClient.getInstances(t.getService()).size()))
//                        .collect(Collectors.toList())),
//                HttpStatus.OK);
    }

    private Page<ScheduleMethodInfoDTO> pageConvert(Page<QuartzMethod> page) {
        List<ScheduleMethodInfoDTO> ScheduleMethodInfoDTOS = new ArrayList<>();
        Page<ScheduleMethodInfoDTO> pageBack = new Page<>();
        pageBack.setNumber(page.getNumber());
        pageBack.setNumberOfElements(page.getNumberOfElements());
        pageBack.setSize(page.getSize());
        pageBack.setTotalElements(page.getTotalElements());
        pageBack.setTotalPages(page.getTotalPages());
        if (page.getContent().isEmpty()) {
            return pageBack;
        } else {
            page.getContent().forEach(t -> {
                ScheduleMethodInfoDTOS.add(new ScheduleMethodInfoDTO(t.getId(), t.getCode(), t.getService(), t.getMethod(), t.getDescription(), discoveryClient.getInstances(t.getService()).size()));
            });
            pageBack.setContent(ScheduleMethodInfoDTOS);
            return pageBack;
        }
    }

    @Override
    public List<ScheduleMethodDTO> getMethodByService(String serviceName) {
        return methodMapper.selectByService(serviceName).stream().map(t -> new ScheduleMethodDTO(t, objectMapper)).collect(Collectors.toList());
    }

    @Override
    public ScheduleMethodParamsDTO getParams(Long id) {
        QuartzMethod method = methodMapper.selectByPrimaryKey(id);
        if (method == null) {
            throw new CommonException("error.scheduleMethod.notExist");
        }
        ScheduleMethodParamsDTO scheduleMethodParamsDTO = methodMapper.selectParamsById(id);
        return new ScheduleMethodParamsDTO(scheduleMethodParamsDTO.getId(),scheduleMethodParamsDTO.getParamsJson(),objectMapper);
    }
}
