package io.choerodon.asgard.app.service;

import io.choerodon.asgard.infra.dto.QuartzTaskDTO;

public interface QuartzJobService {

    /**
     * 添加简单定时任务
     */
    void addJob(QuartzTaskDTO task);

    /**
     * 删除定时任务
     */
    void removeJob(long taskId);

    /**
     * 恢复定时任务
     */
    void resumeJob(long taskId);

    /**
     * 暂停定时任务
     */
    void pauseJob(long taskId);

}
