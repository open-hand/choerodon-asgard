package io.choerodon.asgard.api.service;

import io.choerodon.asgard.domain.QuartzTask;

public interface QuartzJobService {

    /**
     * 添加简单定时任务
     */
    void addSimpleJob(QuartzTask task);

    /**
     * 添加cron定时任务
     */
    void addCronJob(QuartzTask task);

    /**
     * 删除定时任务
     */
    void removeJob(QuartzTask task);

    /**
     * 恢复定时任务
     */
    void resumeJob(QuartzTask task);

    /**
     * 暂停定时任务
     */
    void pauseJob(long taskId);


    /**
     * 检查定时任务是否存在
     */
    void checkJobIsExists(QuartzTask task);

}
