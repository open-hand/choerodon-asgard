package io.choerodon.asgard.api.service;

import io.choerodon.asgard.domain.QuartzTask;
import io.choerodon.asgard.domain.QuartzTaskMember;
import io.choerodon.asgard.domain.SagaInstance;
import io.choerodon.asgard.domain.SagaTaskInstance;

import java.util.List;

/**
 * @author dengyouquan
 **/
public interface NoticeService {
    void sendNotice(final QuartzTask quartzTask, final List<QuartzTaskMember> noticeMember, final String jobStatus);

    void sendSagaFailNotice(final SagaInstance sagaInstance);

    void registerOrgFailNotice(final SagaTaskInstance sagaTaskInstance, final SagaInstance sagaInstance);

}
