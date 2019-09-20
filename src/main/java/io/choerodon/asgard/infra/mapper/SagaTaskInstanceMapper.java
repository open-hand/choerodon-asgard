package io.choerodon.asgard.infra.mapper;

import io.choerodon.asgard.api.vo.PageSagaTaskInstance;
import io.choerodon.asgard.api.vo.SagaTaskInstance;
import io.choerodon.asgard.api.vo.SagaTaskInstanceInfo;
import io.choerodon.asgard.infra.dto.SagaTaskInstanceDTO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface SagaTaskInstanceMapper extends Mapper<SagaTaskInstanceDTO> {

    /**
     * 拉取并发策略为NONE, 状态为RUNNING的消息
     * 当数据库instance值为null或者与传入的instance参数相同，则可以查出
     */
    List<SagaTaskInstance> pollBatchNoneLimit(@Param("service") String service,
                                              @Param("instance") String instance);

    /**
     * 拉取并发策略为TYPE_AND_ID, 状态为RUNNING的消息
     */
    List<SagaTaskInstance> pollBatchTypeAndIdLimit(@Param("service") String service);

    /**
     * 拉取并发策略为TYPE, 状态为RUNNING的消息
     */
    List<SagaTaskInstance> pollBatchTypeLimit(@Param("service") String service);

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

    List<PageSagaTaskInstance> selectAllBySagaInstanceId(@Param("sagaInstanceId") Long instanceId);

    /**
     * 分页查询层级单位下事务实例
     */

    List<SagaTaskInstanceInfo> fulltextSearchTaskInstance(@Param("taskInstanceCode") String taskInstanceCode,
                                                          @Param("status") String status,
                                                          @Param("sagaInstanceCode") String sagaInstanceCode,
                                                          @Param("params") String params,
                                                          @Param("level") String level,
                                                          @Param("sourceId") Long sourceId);

    List<SagaTaskInstanceDTO> selectBySagaInstanceIdAndSeqWithLock(@Param("instanceId") long sagaInstanceId, @Param("seq") int seq);

    List<Long> selectCompletedIdByDate(@Param("fromNowSeconds") long fromNowSeconds, @Param("now") Date now);

    int deleteBatchByIds(@Param("ids") List<Long> ids);
}
