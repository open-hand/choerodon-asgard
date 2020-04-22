package io.choerodon.asgard.app.service.impl;

import io.choerodon.asgard.api.dto.SagaInstanceDTO;
import io.choerodon.asgard.app.service.SagaInstanceC7nService;
import io.choerodon.asgard.api.vo.SagaInstanceFailureVO;
import io.choerodon.asgard.infra.mapper.SagaInstanceC7nMapper;
import io.choerodon.asgard.infra.mapper.SagaInstanceMapper;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * User: Mr.Wang
 * Date: 2020/4/22
 */
@Service
public class SagaInstanceC7nServiceImpl implements SagaInstanceC7nService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SagaInstanceC7nService.class);

    @Autowired
    private SagaInstanceC7nMapper instanceMapper;

    @Override
    public List<SagaInstanceFailureVO> statisticsFailure(String level, Long sourceId, Integer date) {
        String endTime = getTimeStr(null, 1);
        String startTime = getTimeStr(null, date * (-1));

        List<SagaInstanceFailureVO> list;
        if (level != null && !level.equals("site")) {
            list = instanceMapper.statisticsFailure(level, sourceId, startTime, endTime);
        } else {
            list = instanceMapper.statisticsFailure(level, null, startTime, endTime);
        }
        if (CollectionUtils.isEmpty(list)) {
            SagaInstanceFailureVO failureVO = new SagaInstanceFailureVO(getTime(null, date * (-1)), 0L, 0.0, 0L);
            list = new ArrayList<>();
            list.add(failureVO);
        } else if (!getTimeStr(list.get(0).getCreationDate(), 0).equals(getTimeStr(null, date * (-1)))) {
            SagaInstanceFailureVO failureVO = new SagaInstanceFailureVO(getTime(null, date * (-1)), 0L, 0.0, 0L);
            list.add(0, failureVO);
        }

        if (!getTimeStr(list.get(list.size() - 1).getCreationDate(), 0).equals(getTimeStr(null, 0))) {
            SagaInstanceFailureVO failureVO = new SagaInstanceFailureVO(getTime(null, 0), 0L, 0.0, 0L);
            list.add(failureVO);
        }
        return list;
    }

    @Override
    public Page<SagaInstanceDTO> statisticsFailureList(String level, Long sourceId, Integer date, PageRequest pageRequest) {
        String endTime = getTimeStr(null, 1);
        String startTime = getTimeStr(null, date * (-1));

        if (level != null && !level.equals("site")) {
            return PageHelper
                    .doPage(pageRequest,
                            () -> instanceMapper.statisticsFailureList(level, sourceId, startTime, endTime));
        } else {
            return PageHelper
                    .doPage(pageRequest,
                            () -> instanceMapper.statisticsFailureList(level, null, startTime, endTime));
        }
    }

    private Date getTime(Date date, Integer num) {
        date = date == null ? new Date() : date;
        Calendar calStart = Calendar.getInstance();
        calStart.setTime(date);
        calStart.add(Calendar.DAY_OF_MONTH, num);
        return calStart.getTime();
    }

    private String getTimeStr(Date date, Integer num) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(getTime(date, num));
    }
}
