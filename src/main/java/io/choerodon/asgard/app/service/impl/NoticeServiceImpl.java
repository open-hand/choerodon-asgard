package io.choerodon.asgard.app.service.impl;

import java.util.*;

import org.hzero.boot.message.MessageClient;
import org.hzero.boot.message.entity.MessageSender;
import org.hzero.boot.message.entity.Receiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import io.choerodon.asgard.api.vo.RegistrantInfo;
import io.choerodon.asgard.api.vo.User;
import io.choerodon.asgard.api.vo.UserDTO;
import io.choerodon.asgard.app.service.NoticeService;
import io.choerodon.asgard.infra.dto.QuartzTaskDTO;
import io.choerodon.asgard.infra.dto.QuartzTaskMemberDTO;
import io.choerodon.asgard.infra.dto.SagaInstanceDTO;
import io.choerodon.asgard.infra.dto.SagaTaskInstanceDTO;
import io.choerodon.asgard.infra.dto.payload.WebHookUser;
import io.choerodon.asgard.infra.enums.BusinessTypeCode;
import io.choerodon.asgard.infra.enums.MemberType;
import io.choerodon.asgard.infra.feign.IamFeignClient;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;

/**
 * @author dengyouquan
 **/
@Service
public class NoticeServiceImpl implements NoticeService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NoticeServiceImpl.class);

    private static final String JOB_NAME = "jobName";
    private static final String JOB_STATUS = "jobStatus";
    private static final String EVENT_NAME = "组织任务状态";
    private static final String JOB_STATUS_ORGANIZATION = "jobStatusOrganization";

    @Autowired
    private MessageClient messageClient;
    @Autowired
    private IamFeignClient iamFeignClient;


    public static final String REGISTER_ABNORMAL_TEMPLATE = "registerOrganization-abnormal";


    @Override
    @Async("notify-executor")
    public void sendNotice(QuartzTaskDTO quartzTask, List<QuartzTaskMemberDTO> noticeMember, String jobStatus) {
        try {
            //发送通知失败不需要回滚
            MessageSender messageSender = getMessageSender(noticeMember, quartzTask.getName(), quartzTask.getLevel(), quartzTask.getSourceId(), jobStatus, quartzTask);
            messageClient.async().sendMessage(messageSender);
        } catch (CommonException e) {
            LOGGER.info("schedule job send notice fail!", e);
        }
    }


    private MessageSender getMessageSender(List<QuartzTaskMemberDTO> notifyMembers, String jobName, String level, Long sourceId, String jobStatus, QuartzTaskDTO quartzTaskDTO) {
        // 构建消息对象
        MessageSender messageSender = new MessageSender();
        // 消息code
        messageSender.setMessageCode(BusinessTypeCode.getValueByLevel(level).value());
        // 默认为0L,都填0L,可不填写
        messageSender.setTenantId(0L);

        List<UserDTO> needSendNoticeUsers = getNeedSendNoticeUsers(notifyMembers, level, sourceId);

        // 消息参数 消息模板中${projectName}
        Map<String, String> argsMap = new HashMap<>();
        argsMap.put("JOB_NAME", jobName);
        argsMap.put("JOB_STATUS", jobStatus);
        if (JOB_STATUS_ORGANIZATION.equals(BusinessTypeCode.getValueByLevel(level).value())) {
            argsMap.put("objectKind", BusinessTypeCode.getValueByLevel(level).value());
            argsMap.put("eventName", EVENT_NAME);
            argsMap.put("organizationId", sourceId.toString());
            argsMap.put("jobName", jobName);
            argsMap.put("jobStatus", jobStatus);
            argsMap.put("startedAt", String.valueOf(quartzTaskDTO.getStartTime()));
            argsMap.put("finishedAt", String.valueOf(quartzTaskDTO.getLastUpdateDate()));
        }

        // 接收者
        List<Receiver> receiverList = new ArrayList<>();
        List<WebHookUser> webHookUserList = new ArrayList<>();
        needSendNoticeUsers.forEach(user -> {

            WebHookUser webHookUser = new WebHookUser();
            webHookUser.setLoginName(user.getLoginName());
            webHookUser.setUserName(user.getRealName());
            webHookUserList.add(webHookUser);

            Receiver receiver = new Receiver();
            receiver.setUserId(user.getId());
            // 发送邮件消息时 必填
            receiver.setEmail(user.getEmail());
            // 发送短信消息 必填
            receiver.setPhone(user.getPhone());
            // 必填
            receiver.setTargetUserTenantId(user.getOrganizationId());
            receiverList.add(receiver);
        });
        messageSender.setReceiverAddressList(receiverList);
        messageSender.setArgs(argsMap);

        //额外参数，用于逻辑过滤 包括项目id，环境id，devops的消息事件
        Map<String, Object> objectMap = new HashMap<>();
        //发送组织层和项目层消息时必填 当前组织id
        objectMap.put(MessageAdditionalType.PARAM_TENANT_ID.getTypeName(), sourceId);
        messageSender.setAdditionalInformation(objectMap);
        messageClient.async().sendMessage(messageSender);
    }

    @Override
    public void sendSagaFailNotice(SagaInstanceDTO instance) {
        //捕获异常，以免影响saga一致性
        try {

        } catch (Exception e) {
            LOGGER.info("saga instance fail send notice fail, msg: {}", e.getMessage());
        }
        // 构建消息对象
        MessageSender messageSender = new MessageSender();
        // 消息code
        messageSender.setMessageCode("sagaInstanceFail");
        // 默认为0L,都填0L,可不填写
        messageSender.setTenantId(0L);

        // 消息参数 消息模板中${projectName}
        Map<String, String> argsMap = new HashMap<>();
        argsMap.put("sagaInstanceId", instance.getId().toString());
        argsMap.put("sagaCode", instance.getSagaCode());
        argsMap.put("level", instance.getLevel());
        User user = iamFeignClient.queryInfo(instance.getCreatedBy()).getBody();

        // 接收者
        List<Receiver> receiverList = new ArrayList<>();
        Receiver receiver = new Receiver();
        receiver.setUserId(user.getId());
        // 发送邮件消息时 必填
        receiver.setEmail(user.getEmail());
        // 发送短信消息 必填
        receiver.setPhone(user.getPhone());
        // 必填
        receiver.setTargetUserTenantId(user.getOrganizationId());
        receiverList.add(receiver);

        messageSender.setReceiverAddressList(receiverList);
        messageSender.setArgs(argsMap);

        //额外参数，用于逻辑过滤 包括项目id，环境id，devops的消息事件
        Map<String, Object> objectMap = new HashMap<>();
        //发送组织层和项目层消息时必填 当前组织id
        objectMap.put(MessageAdditionalType.PARAM_TENANT_ID.getTypeName(), instance.getSourceId());
        messageSender.setAdditionalInformation(objectMap);
        messageClient.async().sendMessage(messageSender);
    }



    /**
     * 得到需要发送通知的所有用户
     *
     * @param notifyMembers
     * @param level
     * @param sourceId
     * @return
     */
    private List<UserDTO> getNeedSendNoticeUsers(final List<QuartzTaskMemberDTO> notifyMembers, final String level, final Long sourceId) {
        Set<UserDTO> users = new HashSet<>();
        if (notifyMembers == null) return new ArrayList<>(users);
        for (QuartzTaskMemberDTO notifyMember : notifyMembers) {
            if (MemberType.ASSIGNER.value().equals(notifyMember.getMemberType())
                    || MemberType.CREATOR.value().equals(notifyMember.getMemberType())) {
                UserDTO user = new UserDTO();
                user.setId(notifyMember.getMemberId());
                users.add(user);
            }
            if (MemberType.ROLE.value().equals(notifyMember.getMemberType())) {
                users.addAll(getAdministratorUsers(level, sourceId, notifyMember.getMemberId()));
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
     * @param roleId
     * @return
     */
    private List<UserDTO> getAdministratorUsers(String level, Long sourceId, Long roleId) {
        List<UserDTO> users = new ArrayList<>();
        if (ResourceLevel.SITE.value().equals(level)) {
            users = iamFeignClient.pagingQueryUsersByRoleIdOnSiteLevel(roleId).getBody();
        }
        if (ResourceLevel.ORGANIZATION.value().equals(level)) {
            users = iamFeignClient.pagingQueryUsersByRoleIdOnOrganizationLevel(roleId, sourceId).getBody();
        }
        if (ResourceLevel.PROJECT.value().equals(level)) {
            users = iamFeignClient.pagingQueryUsersByRoleIdOnProjectLevel(roleId, sourceId).getBody();
        }
        return users;
    }


    @Override
    public void registerOrgFailNotice(SagaTaskInstanceDTO sagaTaskInstance, SagaInstanceDTO sagaInstance) {
        //feign查询负责人及其组织
        RegistrantInfo registrantInfo = iamFeignClient.queryRegistrantAndAdminId(sagaInstance.getRefId()).getBody();
        LOGGER.info("register failed,ref id:{},registrant info：{}", sagaInstance.getRefId(), registrantInfo);

        // 构建消息对象
        MessageSender messageSender = new MessageSender();
        // 消息code
        messageSender.setMessageCode(REGISTER_ABNORMAL_TEMPLATE);
        // 默认为0L,都填0L,可不填写
        messageSender.setTenantId(0L);


        // 消息参数 消息模板中${projectName}
        Map<String, String> argsMap = new HashMap<>();
        argsMap.put("registrant", registrantInfo.getRealName());
        argsMap.put("organizationId", registrantInfo.getOrganizationId().toString());
        argsMap.put("organizationName", registrantInfo.getOrganizationName());
        argsMap.put("sagaInstanceId", sagaInstance.getSagaCode() + ":" + sagaInstance.getId());
        argsMap.put("sagaTaskInstanceId", sagaTaskInstance.getTaskCode() + ":" + sagaTaskInstance.getId());

        User user = iamFeignClient.queryInfo(registrantInfo.getAdminId()).getBody();

        // 接收者
        List<Receiver> receiverList = new ArrayList<>();
        Receiver receiver = new Receiver();
        receiver.setUserId(user.getId());
        // 发送邮件消息时 必填
        receiver.setEmail(user.getEmail());
        // 发送短信消息 必填
        receiver.setPhone(user.getPhone());
        // 必填
        receiver.setTargetUserTenantId(user.getOrganizationId());
        receiverList.add(receiver);

        messageSender.setReceiverAddressList(receiverList);
        messageSender.setArgs(argsMap);

        //额外参数，用于逻辑过滤 包括项目id，环境id，devops的消息事件
        Map<String, Object> objectMap = new HashMap<>();
        //发送组织层和项目层消息时必填 当前组织id
        objectMap.put(MessageAdditionalType.PARAM_TENANT_ID.getTypeName(), registrantInfo.getOrganizationId());
        messageSender.setAdditionalInformation(objectMap);
        messageClient.async().sendMessage(messageSender);

    }

}
