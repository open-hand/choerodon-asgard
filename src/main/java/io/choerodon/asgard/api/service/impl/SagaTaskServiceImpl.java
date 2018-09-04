package io.choerodon.asgard.api.service.impl;

import io.choerodon.asgard.api.service.SagaTaskService;
import io.choerodon.asgard.domain.SagaTask;
import io.choerodon.asgard.infra.mapper.SagaTaskMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
        SagaTask serviceSagaTask = new SagaTask();
        serviceSagaTask.setService(service);
        List<SagaTask> dbTasks = sagaTaskMapper.select(serviceSagaTask);
        sagaTaskList.forEach(i -> {
            if (StringUtils.isEmpty(i.getCode()) || StringUtils.isEmpty(i.getService()) || i.getSeq() == null) {
                return;
            }
            SagaTask findSaga = findByCode(dbTasks, i.getSagaCode(), i.getCode());
            if (findSaga == null) {
                i.setIsEnabled(true);
                if (sagaTaskMapper.insertSelective(i) != 1) {
                    LOGGER.warn("error.createSagaTask.insert, sagaTask : {}", i);
                }
            } else {
                i.setId(findSaga.getId());
                i.setIsEnabled(true);
                i.setObjectVersionNumber(findSaga.getObjectVersionNumber());
                sagaTaskMapper.updateByPrimaryKeySelective(i);
            }
        });
        dbTasks.stream().filter(t -> t.getIsEnabled() && findByCode(sagaTaskList, t.getSagaCode(), t.getCode()) == null).forEach(t -> {
            t.setIsEnabled(false);
            if (sagaTaskMapper.updateByPrimaryKeySelective(t) != 1) {
                LOGGER.warn("error.createSagaTask.delete, sagaTask : {}", t);
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
