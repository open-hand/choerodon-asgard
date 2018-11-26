package io.choerodon.asgard.api.service.impl;

import io.choerodon.asgard.api.dto.RoleDTO;
import io.choerodon.asgard.api.service.NoticeService;
import io.choerodon.asgard.domain.QuartzTask;
import io.choerodon.asgard.domain.QuartzTaskMember;
import io.choerodon.asgard.infra.enums.BusinessTypeCode;
import io.choerodon.asgard.infra.enums.MemberType;
import io.choerodon.asgard.infra.feign.IamFeignClient;
import io.choerodon.asgard.infra.feign.NotifyFeignClient;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.core.notify.NoticeSendDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author dengyouquan
 **/
@Service
public class NoticeServiceImpl implements NoticeService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NoticeServiceImpl.class);

    private static final String JOB_NAME = "jobName";
    private static final String JOB_STATUS = "jobStatus";

    private NotifyFeignClient notifyFeignClient;

    private IamFeignClient iamFeignClient;

    public NoticeServiceImpl(NotifyFeignClient notifyFeignClient, IamFeignClient iamFeignClient) {
        this.notifyFeignClient = notifyFeignClient;
        this.iamFeignClient = iamFeignClient;
    }

    @Override
    @Async("notify-executor")
    public void sendNotice(QuartzTask quartzTask, List<QuartzTaskMember> noticeMember, String jobStatus) {
        try {
            //发送通知失败不需要回滚
            NoticeSendDTO noticeSendDTO = getNoticeSendDTO(noticeMember, quartzTask.getName(), quartzTask.getLevel(), quartzTask.getSourceId(), jobStatus);
            notifyFeignClient.postNotice(noticeSendDTO);
        } catch (CommonException e) {
            LOGGER.info("schedule job send notice fail!", e);
        }
    }

    private NoticeSendDTO getNoticeSendDTO(final List<QuartzTaskMember> notifyMembers, final String jobName, final String level, final Long sourceId, final String jobStatus) {
        NoticeSendDTO noticeSendDTO = new NoticeSendDTO();
        noticeSendDTO.setSourceId(sourceId);
        noticeSendDTO.setCode(BusinessTypeCode.getValueByLevel(level).value());
        List<NoticeSendDTO.User> users = getNeedSendNoticeUsers(notifyMembers, level, sourceId);
        noticeSendDTO.setTargetUsers(users);
        Map<String, Object> params = new HashMap<>();
        params.put(JOB_NAME, jobName);
        params.put(JOB_STATUS, jobStatus);
        noticeSendDTO.setParams(params);
        return noticeSendDTO;
    }

    /**
     * 得到需要发送通知的所有用户
     *
     * @param notifyMembers
     * @param level
     * @param sourceId
     * @return
     */
    private List<NoticeSendDTO.User> getNeedSendNoticeUsers(final List<QuartzTaskMember> notifyMembers, final String level, final Long sourceId) {
        Set<NoticeSendDTO.User> users = new HashSet<>();
        if (notifyMembers == null) return new ArrayList<>(users);
        for (QuartzTaskMember notifyMember : notifyMembers) {
            if (MemberType.USER.value().equals(notifyMember.getMemberType())) {
                NoticeSendDTO.User user = new NoticeSendDTO.User();
                user.setId(notifyMember.getMemberId());
                users.add(user);
            }
            if (MemberType.ROLE.value().equals(notifyMember.getMemberType())) {
                users.addAll(getAdministratorUsers(level, sourceId));
            }
        }
        //去重
        return new ArrayList<>(users);
    }

    /**
     * 得到组织/项目/全局层对应管理员的所有用户
     *
     * @param level
     * @param sourceId
     * @return
     */
    private List<NoticeSendDTO.User> getAdministratorUsers(String level, Long sourceId) {
        List<NoticeSendDTO.User> users = new ArrayList<>();
        if (ResourceLevel.SITE.value().equals(level)) {
            RoleDTO roleDTO = iamFeignClient.queryByCode(InitRoleCode.SITE_ADMINISTRATOR).getBody();
            users = iamFeignClient.pagingQueryUsersByRoleIdOnSiteLevel(roleDTO.getId(), false).getBody();
        }
        if (ResourceLevel.ORGANIZATION.value().equals(level)) {
            RoleDTO roleDTO = iamFeignClient.queryByCode(InitRoleCode.ORGANIZATION_ADMINISTRATOR).getBody();
            users = iamFeignClient.pagingQueryUsersByRoleIdOnOrganizationLevel(roleDTO.getId(), sourceId, false).getBody();
        }
        if (ResourceLevel.PROJECT.value().equals(level)) {
            RoleDTO roleDTO = iamFeignClient.queryByCode(InitRoleCode.PROJECT_ADMINISTRATOR).getBody();
            users = iamFeignClient.pagingQueryUsersByRoleIdOnProjectLevel(roleDTO.getId(), sourceId, false).getBody();
        }
        return users;
    }
}
