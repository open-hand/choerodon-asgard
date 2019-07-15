package io.choerodon.asgard.app.service;

import io.choerodon.asgard.infra.dto.QuartzTaskDTO;
import io.choerodon.asgard.infra.dto.QuartzTaskMemberDTO;
import io.choerodon.asgard.infra.dto.SagaInstanceDTO;
import io.choerodon.asgard.infra.dto.SagaTaskInstanceDTO;

import java.util.List;

/**
 * @author dengyouquan
 **/
public interface NoticeService {
    void sendNotice(final QuartzTaskDTO quartzTask, final List<QuartzTaskMemberDTO> noticeMember, final String jobStatus);

    void sendSagaFailNotice(final SagaInstanceDTO sagaInstance);

    void registerOrgFailNotice(final SagaTaskInstanceDTO sagaTaskInstance, final SagaInstanceDTO sagaInstance);

}
