package io.choerodon.asgard.app.service;

import java.util.List;

import io.choerodon.asgard.infra.dto.QuartzTaskDTO;
import io.choerodon.asgard.infra.dto.QuartzTaskMemberDTO;
import io.choerodon.asgard.infra.dto.SagaInstanceDTO;
import io.choerodon.asgard.infra.dto.SagaTaskInstanceDTO;

/**
 * @author dengyouquan
 **/
public interface NoticeService {
    void sendNotice(final QuartzTaskDTO quartzTask, final List<QuartzTaskMemberDTO> noticeMember, final String jobStatus);

    void sendSagaFailNotice(final SagaInstanceDTO sagaInstance);

    void sendSagaFailNoticeForTenant(final SagaInstanceDTO sagaInstance);

    void sendSagaFailNoticeForVindicator(SagaInstanceDTO instance);

    void registerOrgFailNotice(final SagaTaskInstanceDTO sagaTaskInstance, final SagaInstanceDTO sagaInstance);

}
