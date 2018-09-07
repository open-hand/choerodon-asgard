package io.choerodon.asgard.api.service.impl;

import io.choerodon.asgard.api.dto.ScheduleTaskInstanceDTO;
import io.choerodon.asgard.api.service.ScheduleTaskInstanceService;
import io.choerodon.asgard.infra.mapper.QuartzTaskInstanceMapper;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ScheduleTaskInstanceServiceImpl implements ScheduleTaskInstanceService {


    private QuartzTaskInstanceMapper quartzTaskInstanceMapper;

    public ScheduleTaskInstanceServiceImpl(QuartzTaskInstanceMapper quartzTaskInstanceMapper) {
        this.quartzTaskInstanceMapper = quartzTaskInstanceMapper;
    }

    @Override
    public ResponseEntity<Page<ScheduleTaskInstanceDTO>> pageQuery(PageRequest pageRequest, String status, String taskName,
                                                                   String exceptionMessage, String params) {
        return new ResponseEntity<>(PageHelper.doPageAndSort(pageRequest,
                () -> quartzTaskInstanceMapper.fulltextSearch(status, taskName, exceptionMessage, params)), HttpStatus.OK);
    }

}
