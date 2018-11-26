package io.choerodon.asgard.api.service;

import io.choerodon.asgard.domain.QuartzTask;
import io.choerodon.asgard.domain.QuartzTaskMember;

import java.util.List;

/**
 * @author dengyouquan
 **/
public interface NoticeService {
    void sendNotice(final QuartzTask quartzTask, final List<QuartzTaskMember> noticeMember, final String jobStatus);
}
