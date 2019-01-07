package io.choerodon.asgard.api.service.impl;

import static io.choerodon.asgard.api.service.impl.SagaInstanceServiceImpl.DB_ERROR;
import static io.choerodon.asgard.api.service.impl.SagaInstanceServiceImpl.ERROR_CODE_SAGA_INSTANCE_NOT_EXIST;
import static java.util.stream.Collectors.groupingBy;
import static org.springframework.transaction.TransactionDefinition.ISOLATION_READ_COMMITTED;
import static org.springframework.transaction.TransactionDefinition.ISOLATION_REPEATABLE_READ;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.StringUtils;

import io.choerodon.asgard.api.dto.SagaTaskInstanceDTO;
import io.choerodon.asgard.api.dto.SagaTaskInstanceInfoDTO;
import io.choerodon.asgard.api.dto.SagaTaskInstanceStatusDTO;
import io.choerodon.asgard.api.service.JsonDataService;
import io.choerodon.asgard.api.service.NoticeService;
import io.choerodon.asgard.api.service.SagaTaskInstanceService;
import io.choerodon.asgard.domain.SagaInstance;
import io.choerodon.asgard.domain.SagaTask;
import io.choerodon.asgard.domain.SagaTaskInstance;
import io.choerodon.asgard.infra.mapper.JsonDataMapper;
import io.choerodon.asgard.infra.mapper.SagaInstanceMapper;
import io.choerodon.asgard.infra.mapper.SagaTaskInstanceMapper;
import io.choerodon.asgard.infra.mapper.SagaTaskMapper;
import io.choerodon.asgard.infra.utils.CommonUtils;
import io.choerodon.asgard.infra.utils.ConvertUtils;
import io.choerodon.asgard.saga.SagaDefinition;
import io.choerodon.asgard.saga.dto.PollSagaTaskInstanceDTO;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.exception.FeignException;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

@Service
public class SagaTaskInstanceServiceImpl implements SagaTaskInstanceService {

    private static final String ERROR_CODE_TASK_INSTANCE_NOT_EXIST = "error.sagaTaskInstance.notExist";

    private static final Logger LOGGER = LoggerFactory.getLogger(SagaTaskInstanceService.class);

    private final ModelMapper modelMapper = new ModelMapper();
    public static final String ORG_REGISTER = "org-register";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private SagaTaskInstanceMapper taskInstanceMapper;
    private SagaInstanceMapper instanceMapper;
    private JsonDataMapper jsonDataMapper;
    private DataSourceTransactionManager transactionManager;
    private NoticeService noticeService;
    private JsonDataService jsonDataService;
    private SagaTaskMapper sagaTaskMapper;

    public SagaTaskInstanceServiceImpl(SagaTaskInstanceMapper taskInstanceMapper,
                                       SagaInstanceMapper instanceMapper,
                                       JsonDataMapper jsonDataMapper,
                                       DataSourceTransactionManager transactionManager,
                                       NoticeService noticeService,
                                       SagaTaskMapper sagaTaskMapper,
                                       JsonDataService jsonDataService) {
        this.taskInstanceMapper = taskInstanceMapper;
        this.instanceMapper = instanceMapper;
        this.jsonDataMapper = jsonDataMapper;
        this.transactionManager = transactionManager;
        this.noticeService = noticeService;
        this.jsonDataService = jsonDataService;
        this.sagaTaskMapper = sagaTaskMapper;
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        modelMapper.addMappings(new PropertyMap<SagaTask, SagaTaskInstance>() {
            @Override
            protected void configure() {
                skip(destination.getId());
                skip(destination.getCreationDate());
                skip(destination.getCreatedBy());
                skip(destination.getLastUpdateDate());
                skip(destination.getLastUpdatedBy());
                skip(destination.getObjectVersionNumber());
            }
        });
    }

    @Override
    public Set<SagaTaskInstanceDTO> pollBatch(final PollSagaTaskInstanceDTO pollBatchDTO) {
        if (pollBatchDTO.getRunningIds() == null) {
            pollBatchDTO.setRunningIds(Collections.emptySet());
        }
        final Set<SagaTaskInstanceDTO> returnList = ConcurrentHashMap.newKeySet();
        //并发策略为NONE的消息拉取。
        taskInstanceMapper.pollBatchNoneLimit(pollBatchDTO.getService(), pollBatchDTO.getInstance()).parallelStream()
                .forEach(t -> {
                    if (returnList.size() >= pollBatchDTO.getMaxPollSize()) {
                        return;
                    }
                    if (pollBatchDTO.getRunningIds().contains(t.getId())) {
                        return;
                    }
                    addToReturnList(returnList, pollBatchDTO.getInstance(), t);
                });

        //并发策略为TYPE_AND_ID的消息拉取。
        taskInstanceMapper.pollBatchTypeAndIdLimit(pollBatchDTO.getService()).stream()
                .collect(groupingBy(t -> t.getRefType() + ":" + t.getRefId())).values()
                .forEach(i -> {
                    if (returnList.size() >= pollBatchDTO.getMaxPollSize()) {
                        return;
                    }
                    addLimit(returnList, i, pollBatchDTO.getInstance(), pollBatchDTO.getRunningIds());
                });
        //并发策略为TYPE的消息拉取。
        taskInstanceMapper.pollBatchTypeLimit(pollBatchDTO.getService()).stream()
                .collect(groupingBy(SagaTaskInstanceDTO::getRefType)).values()
                .forEach(i -> {
                    if (returnList.size() >= pollBatchDTO.getMaxPollSize()) {
                        return;
                    }
                    addLimit(returnList, i, pollBatchDTO.getInstance(), pollBatchDTO.getRunningIds());
                });
        return returnList;
    }

    private void addLimit(final Set<SagaTaskInstanceDTO> returnList,
                          final List<SagaTaskInstanceDTO> list,
                          final String instance,
                          final Set<Long> runningIds) {
        int currentLimitNum = list.get(0).getConcurrentLimitNum();
        list.stream().sorted(Comparator.comparing(SagaTaskInstanceDTO::getId))
                .limit(currentLimitNum)
                .filter(t -> !runningIds.contains(t.getId()))
                .forEach(j -> addToReturnList(returnList, instance, j));
    }

    private void addToReturnList(final Set<SagaTaskInstanceDTO> returnList,
                                 final String instanceLock,
                                 final SagaTaskInstanceDTO j) {
        j.setUserDetails(CommonUtils.readJsonAsUserDetails(objectMapper, j.getUserDetailsJson()));
        Date time = null;
        if (j.getActualStartTime() == null) {
            time = new Date();
        }
        if (StringUtils.isEmpty(j.getInstanceLock())) {
            if (taskInstanceMapper.lockByInstanceAndUpdateStartTime(j.getId(), instanceLock, j.getObjectVersionNumber(), time) == 1) {
                j.setObjectVersionNumber(j.getObjectVersionNumber() + 1);
                returnList.add(j);
            }
        } else if (j.getInstanceLock().equals(instanceLock)) {
            returnList.add(j);
        }
    }

    @Override
    public void updateStatus(final SagaTaskInstanceStatusDTO statusDTO) {
        SagaTaskInstance taskInstance = taskInstanceMapper.selectByPrimaryKey(statusDTO.getId());
        if (taskInstance == null) {
            throw new FeignException(ERROR_CODE_TASK_INSTANCE_NOT_EXIST);
        }
        taskInstance.setObjectVersionNumber(statusDTO.getObjectVersionNumber());
        SagaInstance instance = instanceMapper.selectByPrimaryKey(taskInstance.getSagaInstanceId());
        if (instance == null) {
            throw new FeignException(ERROR_CODE_SAGA_INSTANCE_NOT_EXIST);
        }
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setIsolationLevel(ISOLATION_READ_COMMITTED);
        def.setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = transactionManager.getTransaction(def);
        try {
            if (SagaDefinition.TaskInstanceStatus.COMPLETED.name().equalsIgnoreCase(statusDTO.getStatus())) {
                updateStatusCompleted(taskInstance, statusDTO.getOutput(), instance);
            } else if (SagaDefinition.TaskInstanceStatus.FAILED.name().equalsIgnoreCase(statusDTO.getStatus())) {
                updateStatusFailed(taskInstance, instance, statusDTO.getExceptionMessage());
            }
            transactionManager.commit(status);
        } catch (Exception e) {
            transactionManager.rollback(status);
            throw e;
        }
    }

    private void updateStatusFailed(final SagaTaskInstance taskInstance, final SagaInstance instance, final String exeMsg) {
        //如果已重试次数 >= 最大重试次数，则设置状态为失败，并设置saga实例状态为失败
        if (taskInstance.getRetriedCount() >= taskInstance.getMaxRetryCount()) {
            taskInstance.setStatus(SagaDefinition.TaskInstanceStatus.FAILED.name());
            taskInstance.setExceptionMessage(exeMsg);
            taskInstance.setInstanceLock(null);
            taskInstance.setActualEndTime(new Date());
            if (taskInstanceMapper.updateByPrimaryKey(taskInstance) != 1) {
                throw new FeignException(DB_ERROR);
            }
            instance.setStatus(SagaDefinition.InstanceStatus.FAILED.name());
            instance.setEndTime(new Date());
            instanceMapper.updateByPrimaryKeySelective(instance);
            if (instance.getSagaCode().equalsIgnoreCase(ORG_REGISTER)) {
                List<SagaTaskInstance> sagaTaskInstances = queryByInstanceIdAndSeq(instance.getId(), taskInstance.getSeq());
                noticeService.registerOrgFailNotice(taskInstance, instance, sagaTaskInstances);
            }
            if (instance.getCreatedBy() != 0) {
                noticeService.sendSagaFailNotice(instance);
            }
            //如果已重试次数 < 最大重试次数，则增加重试次数
        } else {
            taskInstanceMapper.increaseRetriedCount(taskInstance.getId());
        }
    }

    private void updateStatusCompleted(final SagaTaskInstance taskInstance, final String outputData, final SagaInstance instance) {
        //更新task实例状态为完成
        taskInstance.setStatus(SagaDefinition.TaskInstanceStatus.COMPLETED.name());
        taskInstance.setOutputDataId(jsonDataService.insertAndGetId(outputData));
        taskInstance.setActualEndTime(new Date());
        if (taskInstanceMapper.updateByPrimaryKeySelective(taskInstance) != 1) {
            throw new FeignException(DB_ERROR);
        }
        List<SagaTaskInstance> sameSeqTasks = taskInstanceMapper.select(new SagaTaskInstance(taskInstance.getSagaInstanceId(), taskInstance.getSeq()));
        //如果同seq下有未完成的实例则直接返回
        if (sameSeqTasks.stream().anyMatch(t -> !SagaDefinition.InstanceStatus.COMPLETED.name().equals(t.getStatus()))) {
            return;
        }
        //如果没有未完成的实例，则继续执行更新SAGA实例状态或者创建下一个seq的task实例
        startNextTaskInstance(sameSeqTasks, taskInstance, instance);
    }

    private void startNextTaskInstance(final List<SagaTaskInstance> sameSeqTasks, final SagaTaskInstance taskInstance, final SagaInstance instance) {
        try {
            // merge此seq下所有task实例的输出结果为同一个json
            String nextInputJson = ConvertUtils.jsonMerge(ConvertUtils.convertToJsonMerge(sameSeqTasks, jsonDataMapper), objectMapper);
            Long nextInputId = jsonDataService.insertAndGetId(nextInputJson);
            // 查询下一步seq的task
            List<SagaTask> nextSeqTasks = sagaTaskMapper.selectNextSeqSagaTasks(instance.getSagaCode(), taskInstance.getSeq());
            // 下一步seq的task为空则更新saga实例状态为完成
            if (nextSeqTasks.isEmpty()) {
                instance.setStatus(SagaDefinition.InstanceStatus.COMPLETED.name());
                instance.setEndTime(new Date());
                instance.setOutputDataId(nextInputId);
                if (instanceMapper.updateByPrimaryKeySelective(instance) != 1) {
                    throw new FeignException("error.updateStatusCompleted.updateInstanceFailed");
                }
                if (instance.getSagaCode().equalsIgnoreCase(ORG_REGISTER)) {
                    noticeService.registerOrgSuccessNotice(instance);
                }
                return;
            }
            // 下一步seq的task不为空则创建相应的task实例
            nextSeqTasks.forEach(t -> {
                SagaTaskInstance sagaTaskInstance = modelMapper.map(t, SagaTaskInstance.class);
                sagaTaskInstance.setSagaInstanceId(instance.getId());
                sagaTaskInstance.setPlannedStartTime(new Date());
                sagaTaskInstance.setInputDataId(nextInputId);
                sagaTaskInstance.setStatus(SagaDefinition.TaskInstanceStatus.WAIT_TO_BE_PULLED.name());
                if (taskInstanceMapper.insertSelective(sagaTaskInstance) != 1) {
                    throw new FeignException(DB_ERROR);
                }
            });
        } catch (IOException e) {
            throw new FeignException("json merge error", e);
        }

    }


    @Override
    public void unlockByInstance(String instance) {
        try {
            taskInstanceMapper.unlockByInstance(instance);
        } catch (Exception e) {
            LOGGER.warn("error.unlockByInstance {}, cause {}", instance, e);
        }
    }

    @Override
    @Transactional
    public void retry(long id) {
        SagaTaskInstance taskInstance = taskInstanceMapper.selectByPrimaryKey(id);
        if (taskInstance == null) {
            throw new CommonException(ERROR_CODE_TASK_INSTANCE_NOT_EXIST);
        }
        SagaInstance sagaInstance = instanceMapper.selectByPrimaryKey(taskInstance.getSagaInstanceId());
        if (sagaInstance == null) {
            throw new CommonException("error.sagaInstance.notExist");
        }
        sagaInstance.setStatus(SagaDefinition.InstanceStatus.RUNNING.name());
        sagaInstance.setEndTime(null);
        instanceMapper.updateByPrimaryKey(sagaInstance);
        taskInstance.setStatus(SagaDefinition.TaskInstanceStatus.RUNNING.name());
        taskInstanceMapper.updateByPrimaryKeySelective(taskInstance);
    }

    @Override
    public void unlockById(long id) {
        SagaTaskInstance taskInstance = taskInstanceMapper.selectByPrimaryKey(id);
        if (taskInstance == null) {
            throw new CommonException(ERROR_CODE_TASK_INSTANCE_NOT_EXIST);
        }
        taskInstance.setInstanceLock(null);
        taskInstanceMapper.updateByPrimaryKey(taskInstance);
    }

    @Override
    public void forceFailed(long id) {
        SagaTaskInstance taskInstance = taskInstanceMapper.selectByPrimaryKey(id);
        if (taskInstance == null) {
            throw new FeignException(ERROR_CODE_TASK_INSTANCE_NOT_EXIST);
        }
        SagaInstance instance = instanceMapper.selectByPrimaryKey(taskInstance.getSagaInstanceId());
        if (instance == null) {
            throw new FeignException("error.sagaInstance.notExist");
        }
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setIsolationLevel(ISOLATION_REPEATABLE_READ);
        def.setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = transactionManager.getTransaction(def);
        try {
            updateStatusFailed(taskInstance, instance, "manual force failed");
            transactionManager.commit(status);
        } catch (Exception e) {
            transactionManager.rollback(status);
            throw e;
        }
    }

    @Override
    public ResponseEntity<Page<SagaTaskInstanceInfoDTO>> pageQuery(PageRequest pageRequest, String sagaInstanceCode, String status, String taskInstanceCode, String params, String level, Long sourceId) {
        return new ResponseEntity<>(PageHelper.doPageAndSort(pageRequest,
                () -> taskInstanceMapper.fulltextSearchTaskInstance(sagaInstanceCode, status, taskInstanceCode, params, level, sourceId)), HttpStatus.OK);
    }

    @Override
    public SagaTaskInstanceDTO query(long id) {
        SagaTaskInstance sagaTaskInstance = taskInstanceMapper.selectByPrimaryKey(id);
        if (sagaTaskInstance == null) {
            throw new FeignException(ERROR_CODE_TASK_INSTANCE_NOT_EXIST);
        }
        return modelMapper.map(sagaTaskInstance, SagaTaskInstanceDTO.class);
    }

    @Override
    public List<SagaTaskInstance> queryByInstanceIdAndSeq(Long sagaInatanceId, Integer seq) {
        List<SagaTaskInstance> sagaTaskInstances = taskInstanceMapper.selectAllBySagaInstanceId(sagaInatanceId);
        return sagaTaskInstances.stream().filter(s -> s.getSeq().equals(seq)).collect(Collectors.toList());
    }
}
