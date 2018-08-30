package io.choerodon.asgard.infra.mapper;

import io.choerodon.asgard.api.dto.SagaTaskInstanceDTO;
import io.choerodon.asgard.domain.SagaTaskInstance;
import io.choerodon.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface SagaTaskInstanceMapper extends BaseMapper<SagaTaskInstance> {

    /**
     * 拉取并发策略为NONE, 状态为RUNNING的消息
     * 当数据库instance值为null或者与传入的instance参数相同，则可以查出
     */
    List<SagaTaskInstanceDTO> pollBatchNoneLimit(@Param("sagaCode") String sagaCode,
                                                @Param("taskCode") String taskCode,
                                                @Param("instance") String instance);

    /**
     * 拉取并发策略为TYPE_AND_ID, 状态为RUNNING的消息
     */
    List<SagaTaskInstanceDTO> pollBatchTypeAndIdLimit(@Param("sagaCode") String sagaCode,
                                                      @Param("taskCode") String taskCode);
    /**
     * 拉取并发策略为TYPE, 状态为RUNNING的消息
     */
    List<SagaTaskInstanceDTO> pollBatchTypeLimit(@Param("sagaCode") String sagaCode,
                                                 @Param("taskCode") String taskCode);

    /**
     * 设置消息的实例锁，并更新实际开始时间
     * 当传入的objectVersionNumber和数据库相同且instance_lock为null，才进行更新
     */
    int lockByInstanceAndUpdateStartTime(@Param("id") long id,
                                         @Param("instance") String instance,
                                         @Param("number") Long objectVersionNumber,
                                         @Param("time") Date date);

    /**
     * 增加重试次数
     */
    void increaseRetriedCount(@Param("id") long id);

    /**
     * 去除消息的实例锁，让其他实例可以获取到该消息
     * 仅操作状态为RUNNING的消息
     */
    int unlockByInstance(@Param("instance") String instance);

    List<SagaTaskInstanceDTO> selectAllBySagaInstanceId(@Param("sagaInstanceId") Long instanceId);

}
