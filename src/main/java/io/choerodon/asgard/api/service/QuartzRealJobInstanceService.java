package io.choerodon.asgard.api.service;

public abstract class QuartzRealJobInstanceService implements QuartzRealJobService {

    @Override
    public void triggerEvent(long taskId) {
        verifyUntreatedInstance(taskId);
        createInstance(taskId);
    }

    /**
     * 未消费的消息处理
     * @param taskId 定时任务的id
     */
    public abstract void verifyUntreatedInstance (long taskId);

    public abstract void createInstance(long taskId);
}
