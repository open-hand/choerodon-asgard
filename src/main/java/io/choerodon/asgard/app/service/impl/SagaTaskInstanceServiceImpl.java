package io.choerodon.asgard.app.service.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.choerodon.asgard.api.vo.PageSagaTaskInstance;
import io.choerodon.asgard.api.vo.SagaTaskInstance;
import io.choerodon.asgard.api.vo.SagaTaskInstanceInfo;
import io.choerodon.asgard.api.vo.SagaTaskInstanceStatus;
import io.choerodon.asgard.app.eventhandler.SagaInstanceEventPublisher;
import io.choerodon.asgard.app.service.JsonDataService;
import io.choerodon.asgard.app.service.NoticeService;
import io.choerodon.asgard.app.service.SagaTaskInstanceService;
import io.choerodon.asgard.infra.dto.SagaInstanceDTO;
import io.choerodon.asgard.infra.dto.SagaTaskDTO;
import io.choerodon.asgard.infra.dto.SagaTaskInstanceDTO;
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

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.choerodon.mybatis.pagehelper.domain.PageRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static io.choerodon.asgard.app.service.impl.SagaInstanceServiceImpl.DB_ERROR;
import static io.choerodon.asgard.app.service.impl.SagaInstanceServiceImpl.ERROR_CODE_SAGA_INSTANCE_NOT_EXIST;

import static java.util.stream.Collectors.groupingBy;

import static org.springframework.transaction.TransactionDefinition.ISOLATION_READ_UNCOMMITTED;

@Service
public class SagaTaskInstanceServiceImpl implements SagaTaskInstanceService {

    private static final String ERROR_CODE_TASK_INSTANCE_NOT_EXIST = "error.sagaTaskInstance.notExist";

    private static final Logger LOGGER = LoggerFactory.getLogger(SagaTaskInstanceService.class);

    private final ModelMapper modelMapper = new ModelMapper();

    private static final String ORG_REGISTER = "register-org";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private SagaTaskInstanceMapper taskInstanceMapper;
    private SagaInstanceMapper instanceMapper;
    private JsonDataMapper jsonDataMapper;
    private DataSourceTransactionManager transactionManager;
    private NoticeService noticeService;
    private JsonDataService jsonDataService;
    private SagaTaskMapper sagaTaskMapper;
    private SagaInstanceEventPublisher sagaInstanceEventPublisher;

    public SagaTaskInstanceServiceImpl(SagaTaskInstanceMapper taskInstanceMapper,
                                       SagaInstanceMapper instanceMapper,
                                       JsonDataMapper jsonDataMapper,
                                       DataSourceTransactionManager transactionManager,
                                       NoticeService noticeService,
                                       SagaTaskMapper sagaTaskMapper,
                                       JsonDataService jsonDataService,
                                       SagaInstanceEventPublisher sagaInstanceEventPublisher) {
        this.taskInstanceMapper = taskInstanceMapper;
        this.instanceMapper = instanceMapper;
        this.jsonDataMapper = jsonDataMapper;
        this.transactionManager = transactionManager;
        this.noticeService = noticeService;
        this.jsonDataService = jsonDataService;
        this.sagaTaskMapper = sagaTaskMapper;
        this.sagaInstanceEventPublisher = sagaInstanceEventPublisher;
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        modelMapper.addMappings(new PropertyMap<SagaTaskDTO, SagaTaskInstanceDTO>() {
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
    public Set<SagaTaskInstance> pollBatch(final PollSagaTaskInstanceDTO pollBatchDTO) {
        if (pollBatchDTO.getRunningIds() == null) {
            pollBatchDTO.setRunningIds(Collections.emptySet());
        }
        final Set<SagaTaskInstance> returnList = ConcurrentHashMap.newKeySet();
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
                .collect(groupingBy(SagaTaskInstance::getRefType)).values()
                .forEach(i -> {
                    if (returnList.size() >= pollBatchDTO.getMaxPollSize()) {
                        return;
                    }
                    addLimit(returnList, i, pollBatchDTO.getInstance(), pollBatchDTO.getRunningIds());
                });
        return returnList;
    }

    private void addLimit(final Set<SagaTaskInstance> returnList,
                          final List<SagaTaskInstance> list,
                          final String instance,
                          final Set<Long> runningIds) {
        int currentLimitNum = list.get(0).getConcurrentLimitNum();
        list.stream().sorted(Comparator.comparing(SagaTaskInstance::getId))
                .limit(currentLimitNum)
                .filter(t -> !runningIds.contains(t.getId()))
                .forEach(j -> addToReturnList(returnList, instance, j));
    }

    private void addToReturnList(final Set<SagaTaskInstance> returnList,
                                 final String instanceLock,
                                 final SagaTaskInstance j) {
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
    public String updateStatus(final SagaTaskInstanceStatus statusDTO) {
        LOGGER.info("<<<<<<<<<<<<<<<<<<<<<<<<<<更新执行状态>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        SagaTaskInstanceDTO taskInstance = taskInstanceMapper.selectByPrimaryKey(statusDTO.getId());
        if (taskInstance == null) {
            throw new FeignException(ERROR_CODE_TASK_INSTANCE_NOT_EXIST);
        }
        taskInstance.setObjectVersionNumber(statusDTO.getObjectVersionNumber());
        SagaInstanceDTO instance = instanceMapper.selectByPrimaryKey(taskInstance.getSagaInstanceId());
        if (instance == null) {
            throw new FeignException(ERROR_CODE_SAGA_INSTANCE_NOT_EXIST);
        }
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setIsolationLevel(ISOLATION_READ_UNCOMMITTED);
        def.setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = transactionManager.getTransaction(def);
        try {
            if (SagaDefinition.TaskInstanceStatus.COMPLETED.name().equalsIgnoreCase(statusDTO.getStatus())) {
                LOGGER.info("<<<<<<<<<<<<<<<<<<<<<<<<<<执行状态：{}>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>", SagaDefinition.TaskInstanceStatus.COMPLETED.name());
                updateStatusCompleted(taskInstance, statusDTO.getOutput(), instance);
            } else if (SagaDefinition.TaskInstanceStatus.FAILED.name().equalsIgnoreCase(statusDTO.getStatus())) {
                LOGGER.info("<<<<<<<<<<<<<<<<<<<<<<<<<<执行状态：{}>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>", SagaDefinition.TaskInstanceStatus.FAILED.name());
                updateStatusFailed(taskInstance, instance, statusDTO.getExceptionMessage(), false);
            }
            transactionManager.commit(status);
        } catch (Exception e) {
            transactionManager.rollback(status);
            throw e;
        }
        return taskInstance.getStatus();
    }

    @Override
    public void updateStatusFailureCallback(Long sagaTaskInstanceId, String status) {
        SagaTaskInstanceDTO taskInstance = taskInstanceMapper.selectByPrimaryKey(sagaTaskInstanceId);
        if (taskInstance == null) {
            throw new FeignException(ERROR_CODE_TASK_INSTANCE_NOT_EXIST);
        }
        taskInstance.setFailureCallbackStatus(status);
        taskInstanceMapper.updateByPrimaryKey(taskInstance);
    }

    private void updateStatusFailed(final SagaTaskInstanceDTO taskInstance, final SagaInstanceDTO instance, final String exeMsg, final boolean isForceFailed) {
        //如果已重试次数 >= 最大重试次数，则设置状态为失败，并设置saga实例状态为失败
        if (isForceFailed || taskInstance.getRetriedCount() >= taskInstance.getMaxRetryCount()) {
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
                noticeService.registerOrgFailNotice(taskInstance, instance);
            }
            if (instance.getCreatedBy() != 0) {
                noticeService.sendSagaFailNotice(instance);
            }
            //如果已重试次数 < 最大重试次数，则增加重试次数
        } else {
            taskInstanceMapper.increaseRetriedCount(taskInstance.getId());
            //如果可重试，则通知相应的服务来立即拉取信息
            SagaTaskDTO example = new SagaTaskDTO();
            example.setSagaCode(taskInstance.getSagaCode());
            example.setCode(taskInstance.getTaskCode());
            sagaInstanceEventPublisher.sagaTaskInstanceEvent(sagaTaskMapper.selectOne(example).getService());
        }
    }

    private void updateStatusCompleted(final SagaTaskInstanceDTO taskInstance, final String outputData, final SagaInstanceDTO instance) {
        List<SagaTaskInstanceDTO> sameSeqTasks = taskInstanceMapper.selectBySagaInstanceIdAndSeqWithLock(taskInstance.getSagaInstanceId(), taskInstance.getSeq());
        //更新task实例状态为完成
        taskInstance.setStatus(SagaDefinition.TaskInstanceStatus.COMPLETED.name()).setOutputDataId(jsonDataService.insertAndGetId(outputData)).setActualEndTime(new Date());
        if (taskInstanceMapper.updateByPrimaryKeySelective(taskInstance) != 1) {
            throw new FeignException(DB_ERROR);
        }
        sameSeqTasks.stream().filter(t -> t.getId().equals(taskInstance.getId())).forEach(i -> {
            i.setStatus(taskInstance.getStatus());
            i.setOutputDataId(taskInstance.getOutputDataId());
        });
        //如果同seq下有未完成的实例则直接返回
        if (sameSeqTasks.stream().anyMatch(t -> !SagaDefinition.InstanceStatus.COMPLETED.name().equals(t.getStatus()))) {
            return;
        }
        //如果没有未完成的实例，则继续执行更新SAGA实例状态或者创建下一个seq的task实例
        startNextTaskInstance(sameSeqTasks, taskInstance, instance);
    }

    private void startNextTaskInstance(final List<SagaTaskInstanceDTO> sameSeqTasks, final SagaTaskInstanceDTO taskInstance, final SagaInstanceDTO instance) {
        try {
            // merge此seq下所有task实例的输出结果为同一个json
            String nextInputJson = ConvertUtils.jsonMerge(ConvertUtils.convertToJsonMerge(sameSeqTasks, jsonDataMapper), objectMapper);
            Long nextInputId = jsonDataService.insertAndGetId(nextInputJson);
            // 查询下一步seq的task
            List<SagaTaskDTO> nextSeqTasks = sagaTaskMapper.selectNextSeqSagaTasks(instance.getSagaCode(), taskInstance.getSeq());
            // 下一步seq的task为空则更新saga实例状态为完成
            if (nextSeqTasks.isEmpty()) {
                instance.setStatus(SagaDefinition.InstanceStatus.COMPLETED.name());
                instance.setEndTime(new Date());
                instance.setOutputDataId(nextInputId);
                if (instanceMapper.updateByPrimaryKeySelective(instance) != 1) {
                    throw new FeignException("error.updateStatusCompleted.updateInstanceFailed");
                }
                return;
            }
            // 下一步seq的task不为空则创建相应的task实例
            nextSeqTasks.forEach(t -> {
                SagaTaskInstanceDTO sagaTaskInstance = modelMapper.map(t, SagaTaskInstanceDTO.class);
                sagaTaskInstance.setSagaInstanceId(instance.getId());
                sagaTaskInstance.setPlannedStartTime(new Date());
                sagaTaskInstance.setInputDataId(nextInputId);
                sagaTaskInstance.setStatus(SagaDefinition.TaskInstanceStatus.WAIT_TO_BE_PULLED.name());
                if (taskInstanceMapper.insertSelective(sagaTaskInstance) != 1) {
                    throw new FeignException(DB_ERROR);
                } else {
                    //通知相应的服务来立即拉取信息
                    sagaInstanceEventPublisher.sagaTaskInstanceEvent(t.getService());
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
        SagaTaskInstanceDTO taskInstance = taskInstanceMapper.selectByPrimaryKey(id);
        if (taskInstance == null) {
            throw new CommonException(ERROR_CODE_TASK_INSTANCE_NOT_EXIST);
        }
        SagaInstanceDTO sagaInstance = instanceMapper.selectByPrimaryKey(taskInstance.getSagaInstanceId());
        if (sagaInstance == null) {
            throw new CommonException("error.sagaInstance.notExist");
        }
        sagaInstance.setStatus(SagaDefinition.InstanceStatus.RUNNING.name());
        sagaInstance.setEndTime(null);
        instanceMapper.updateByPrimaryKey(sagaInstance);
        taskInstance.setStatus(SagaDefinition.TaskInstanceStatus.WAIT_TO_BE_PULLED.name());
        taskInstanceMapper.updateByPrimaryKeySelective(taskInstance);
        //通知相应的服务来立即拉取信息
        SagaTaskDTO example = new SagaTaskDTO();
        example.setSagaCode(taskInstance.getSagaCode());
        example.setCode(taskInstance.getTaskCode());
        sagaInstanceEventPublisher.sagaTaskInstanceEvent(sagaTaskMapper.selectOne(example).getService());
    }

    @Override
    public void unlockById(long id) {
        SagaTaskInstanceDTO taskInstance = taskInstanceMapper.selectByPrimaryKey(id);
        if (taskInstance == null) {
            throw new CommonException(ERROR_CODE_TASK_INSTANCE_NOT_EXIST);
        }
        taskInstance.setInstanceLock(null);
        taskInstanceMapper.updateByPrimaryKey(taskInstance);
    }

    @Override
    public void forceFailed(long id) {
        forceFailed(id, "manual force failed");
    }

    private void forceFailed(long id, String exeMsg) {
        SagaTaskInstanceDTO taskInstance = taskInstanceMapper.selectByPrimaryKey(id);
        if (taskInstance == null) {
            throw new FeignException(ERROR_CODE_TASK_INSTANCE_NOT_EXIST);
        }
        SagaInstanceDTO instance = instanceMapper.selectByPrimaryKey(taskInstance.getSagaInstanceId());
        if (instance == null) {
            throw new FeignException("error.sagaInstance.notExist");
        }
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setIsolationLevel(ISOLATION_READ_UNCOMMITTED);
        def.setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = transactionManager.getTransaction(def);
        try {
            updateStatusFailed(taskInstance, instance, exeMsg, true);
            transactionManager.commit(status);
        } catch (Exception e) {
            transactionManager.rollback(status);
            throw e;
        }
    }


    @Override
    public ResponseEntity<Page<SagaTaskInstanceInfo>> pageQuery(PageRequest pageable, String taskInstanceCode, String sagaInstanceCode, String status, String params, String level, Long sourceId) {
        return new ResponseEntity<Page<SagaTaskInstanceInfo>>(
                PageHelper.doPageAndSort(
                        pageable,
                        () -> taskInstanceMapper.fulltextSearchTaskInstance(taskInstanceCode, status, sagaInstanceCode, params, level, sourceId)), HttpStatus.OK);
    }

    @Override
    public SagaTaskInstance query(long id) {
        SagaTaskInstanceDTO sagaTaskInstance = taskInstanceMapper.selectByPrimaryKey(id);
        if (sagaTaskInstance == null) {
            throw new FeignException(ERROR_CODE_TASK_INSTANCE_NOT_EXIST);
        }
        return modelMapper.map(sagaTaskInstance, SagaTaskInstance.class);
    }

    @Override
    public List<PageSagaTaskInstance> queryByInstanceIdAndSeq(Long sagaInstanceId, Integer seq) {
        return taskInstanceMapper.selectAllBySagaInstanceId(sagaInstanceId).stream()
                .filter(s -> s.getSeq().equals(seq)).collect(Collectors.toList());
    }

    @Override
    public void failedLockedInstance(PollSagaTaskInstanceDTO pollBatchDTO) {
        List<SagaTaskInstanceDTO> list = taskInstanceMapper.queryLockedInstance(pollBatchDTO.getService(), pollBatchDTO.getInstance());
        if (!CollectionUtils.isEmpty(list)) {
            list.forEach(t -> forceFailed(t.getId(), "execution timeout"));
        }
    }

    @Override
    public void retrySagaTask(Long projectId, List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) return;
        ids.forEach(this::retry);
    }
}
