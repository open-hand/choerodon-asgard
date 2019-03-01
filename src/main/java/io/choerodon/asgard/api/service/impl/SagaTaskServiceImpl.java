package io.choerodon.asgard.api.service.impl;

import io.choerodon.asgard.api.service.SagaTaskService;
import io.choerodon.asgard.domain.SagaTask;
import io.choerodon.asgard.infra.mapper.SagaTaskMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class SagaTaskServiceImpl implements SagaTaskService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SagaTaskService.class);

    private SagaTaskMapper sagaTaskMapper;

    public SagaTaskServiceImpl(SagaTaskMapper sagaTaskMapper) {
        this.sagaTaskMapper = sagaTaskMapper;
    }

    @Override
    public void createSagaTaskList(final List<SagaTask> sagaTaskList, final String service) {
        List<SagaTask> copyTaskList = new ArrayList<>(sagaTaskList);
        SagaTask serviceSagaTask = new SagaTask();
        serviceSagaTask.setService(service);
        List<SagaTask> dbTasks = sagaTaskMapper.select(serviceSagaTask);
        sagaTaskList.forEach(i -> {
            if (StringUtils.isEmpty(i.getCode()) || StringUtils.isEmpty(i.getService()) || i.getSeq() == null) {
                return;
            }
            SagaTask dbSagaTask = findByCode(dbTasks, i.getSagaCode(), i.getCode());
            if (dbSagaTask == null) {
                i.setIsEnabled(true);
                if (sagaTaskMapper.insertSelective(i) != 1) {
                    LOGGER.error("insert saga task error: {}", i);
                } else {
                    LOGGER.info("insert saga task: {}", i);
                }
            } else {
                i.setId(dbSagaTask.getId());
                i.setIsEnabled(true);
                i.setObjectVersionNumber(dbSagaTask.getObjectVersionNumber());
                if (sagaTaskMapper.updateByPrimaryKeySelective(i) == 1) {
                    LOGGER.info("update saga task: {}", i);
                }
            }
        });
        disableSagaTask(dbTasks, copyTaskList);
    }

    private void disableSagaTask(final List<SagaTask> dbTasks, final List<SagaTask> sagaTaskList) {
        dbTasks.stream().filter(t -> t.getIsEnabled() && findByCode(sagaTaskList, t.getSagaCode(), t.getCode()) == null).forEach(t -> {
            t.setIsEnabled(false);
            if (sagaTaskMapper.updateByPrimaryKeySelective(t) != 1) {
                LOGGER.error("update saga task disabled error: {}", t);
            } else {
                LOGGER.info("update saga task disabled: {}", t);
            }
        });
    }

    private SagaTask findByCode(final List<SagaTask> sagaTaskList, final String sagaCode, final String code) {
        for (SagaTask sagaTask : sagaTaskList) {
            if (StringUtils.isEmpty(sagaTask.getCode()) || StringUtils.isEmpty(sagaTask.getService()) || sagaTask.getSeq() == null) {
                return null;
            }
            if (sagaTask.getCode().equals(code) && sagaTask.getSagaCode().equals(sagaCode)) {
                return sagaTask;
            }
        }
        return null;
    }

}
