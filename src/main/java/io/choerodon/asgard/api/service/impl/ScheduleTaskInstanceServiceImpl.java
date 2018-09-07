package io.choerodon.asgard.api.service.impl;

import io.choerodon.asgard.api.dto.ScheduleTaskInstanceDTO;
import io.choerodon.asgard.api.service.ScheduleTaskInstanceService;
import io.choerodon.asgard.infra.mapper.QuartzTasKInstanceMapper;
import io.choerodon.asgard.infra.mapper.QuartzTaskMapper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class ScheduleTaskInstanceServiceImpl implements ScheduleTaskInstanceService {

    private static final String ERROR_QUARTZ_TASK_NOT_EXIST = "error.quartz.taskName.notExist";

    private QuartzTasKInstanceMapper quartzTaskInstanceMapper;
    private QuartzTaskMapper quartzTaskMapper;

    public ScheduleTaskInstanceServiceImpl(QuartzTasKInstanceMapper quartzTaskInstanceMapper,
                                           QuartzTaskMapper quartzTaskMapper) {
        this.quartzTaskInstanceMapper = quartzTaskInstanceMapper;
        this.quartzTaskMapper = quartzTaskMapper;
    }

    @Override
    public ResponseEntity<Page<ScheduleTaskInstanceDTO>> pageQuery(PageRequest pageRequest, String status, String taskName,
                                                                   String exceptionMessage, String params) {
        List<Long> list = quartzTaskMapper.selectTaskIdByName(taskName);
        Long taskId = list.isEmpty() ? null : list.get(0);
        if (StringUtils.isEmpty(taskId)) {
            throw new CommonException(ERROR_QUARTZ_TASK_NOT_EXIST);
        }
        return new ResponseEntity<>(PageHelper.doPageAndSort(pageRequest,
                () -> quartzTaskInstanceMapper.fulltextSearch(status, taskName, exceptionMessage, params)), HttpStatus.OK);
    }

}
