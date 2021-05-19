package io.choerodon.asgard.app.service.impl;

import static io.choerodon.asgard.infra.enums.BusinessTypeCode.*;

import java.util.*;

import org.hzero.boot.message.MessageClient;
import org.hzero.boot.message.entity.MessageSender;
import org.hzero.boot.message.entity.Receiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import io.choerodon.asgard.api.vo.*;
import io.choerodon.asgard.app.service.NoticeService;
import io.choerodon.asgard.infra.dto.QuartzTaskDTO;
import io.choerodon.asgard.infra.dto.QuartzTaskMemberDTO;
import io.choerodon.asgard.infra.dto.SagaInstanceDTO;
import io.choerodon.asgard.infra.dto.SagaTaskInstanceDTO;
import io.choerodon.asgard.infra.enums.BusinessTypeCode;
import io.choerodon.asgard.infra.enums.MemberType;
import io.choerodon.asgard.infra.feign.IamFeignClient;
import io.choerodon.asgard.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.core.enums.MessageAdditionalType;
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
    private static final String JOB_STATUS_ORGANIZATION = "JOBSTATUSORGANIZATION";
    private static final String EVENT_NAME_SITE = "平台任务状态通知";
    private static final String JOB_STATUS_SITE = "JOBSTATUSSITE";

    private static final String NO_SEND_WEBHOOK = "NoSendWebHook";
    private static final String NO_SEND_WEB = "NoSendWeb";
    private static final String NO_SEND_EMAIL = "NoSendEmail";
    private static final String NO_SEND_SMS = "NoSendSms";
    private static final String IAM_CREATE_ORG_USER = "iam-create-org-user";
    private static final String IAM_CREATE_USER = "iam-create-user";
    private static final String ORG_CREATE_ORGANIZATION = "org-create-organization";
    private static final String IAM_CREATE_PROJECT = "iam-create-project";
    private static final Map<String, String> mapEvent = new HashMap<>();

    static {
        mapEvent.put(IAM_CREATE_ORG_USER, VINDICATOR_CREATE_USER_FAILED.value());
        mapEvent.put(IAM_CREATE_USER, VINDICATOR_CREATE_USER_FAILED.value());
        mapEvent.put(ORG_CREATE_ORGANIZATION, VINDICATOR_CREATE_ORGANIZATION_FAILED.value());
        mapEvent.put(IAM_CREATE_PROJECT, VINDICATOR_CREATE_PROJECT_FAILED.value());
    }


    @Autowired
    private MessageClient messageClient;
    @Autowired
    private IamFeignClient iamFeignClient;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;


    @Override
    @Async("notify-executor")
    public void sendNotice(QuartzTaskDTO quartzTask, List<QuartzTaskMemberDTO> noticeMember, String jobStatus) {
        try {
            //发送通知失败不需要回滚
            //获取通知对象
            List<UserDTO> needSendNoticeUsers = getNeedSendNoticeUsers(noticeMember, quartzTask.getLevel(), quartzTask.getSourceId());
            MessageSender baseMessageSender = getBaseMessageSender(quartzTask.getName(), quartzTask.getLevel(), quartzTask.getSourceId(), jobStatus, quartzTask);
            if (!CollectionUtils.isEmpty(needSendNoticeUsers)) {
                needSendNoticeUsers.forEach(userDTO -> {
                    //除开webhook的类型的
                    MessageSender otherSender = getOtherMessageSender(userDTO, baseMessageSender, quartzTask.getSourceId());
                    messageClient.async().sendMessage(otherSender);
                });

            }
            //发送webhook
            MessageSender webhookSender = getWebHookMessageSender(baseMessageSender, quartzTask.getSourceId());
            messageClient.async().sendMessage(webhookSender);
        } catch (CommonException e) {
            LOGGER.info("schedule job send notice fail!", e);
        }
    }

    private MessageSender getWebHookMessageSender(MessageSender messageSender, Long sourceId) {
        //额外参数，用于逻辑过滤 包括项目id，环境id，devops的消息事件
        Map<String, Object> objectMap = new HashMap<>();
        //发送组织层和项目层消息时必填 当前组织id
        objectMap.put(MessageAdditionalType.PARAM_TENANT_ID.getTypeName(), sourceId);
        objectMap.put(NO_SEND_WEB, NO_SEND_WEB);
        objectMap.put(NO_SEND_EMAIL, NO_SEND_EMAIL);
        objectMap.put(NO_SEND_SMS, NO_SEND_SMS);
        messageSender.setAdditionalInformation(objectMap);
        return messageSender;

    }


    private MessageSender getOtherMessageSender(UserDTO userDTO, MessageSender messageSender, Long sourceId) {
        messageSender.getArgs().put("userName", userDTO.getRealName());
        // 接收者
        List<Receiver> receiverList = new ArrayList<>();

        Receiver receiver = new Receiver();
        receiver.setUserId(userDTO.getId());
        // 发送邮件消息时 必填
        receiver.setEmail(userDTO.getEmail());
        // 发送短信消息 必填
        receiver.setPhone(userDTO.getPhone());
        // 必填
        receiver.setTargetUserTenantId(userDTO.getOrganizationId());
        receiverList.add(receiver);

        messageSender.setReceiverAddressList(receiverList);

        //额外参数，用于逻辑过滤 包括项目id，环境id，devops的消息事件
        Map<String, Object> objectMap = new HashMap<>();
        //发送组织层和项目层消息时必填 当前组织id
        objectMap.put(MessageAdditionalType.PARAM_TENANT_ID.getTypeName(), sourceId);
        objectMap.put(NO_SEND_WEBHOOK, NO_SEND_WEBHOOK);
        messageSender.setAdditionalInformation(objectMap);
        return messageSender;
    }

    private MessageSender getBaseMessageSender(String jobName, String level, Long sourceId, String jobStatus, QuartzTaskDTO quartzTaskDTO) {

        MessageSender messageSender = new MessageSender();
        // 消息code
        messageSender.setMessageCode(BusinessTypeCode.getValueByLevel(level.toUpperCase()).value());
        // 默认为0L,都填0L,可不填写
        messageSender.setTenantId(0L);

        // 消息参数 消息模板中${projectName}
        Map<String, String> argsMap = new HashMap<>();
        argsMap.put("JOB_NAME", jobName);
        argsMap.put("JOB_STATUS", jobStatus);

        argsMap.put("objectKind", BusinessTypeCode.getValueByLevel(level.toUpperCase()).value());
        argsMap.put("organizationId", sourceId.toString());
        argsMap.put("jobName", jobName);
        argsMap.put("jobStatus", jobStatus);
        argsMap.put("startedAt", String.valueOf(quartzTaskDTO.getStartTime()));
        argsMap.put("finishedAt", String.valueOf(quartzTaskDTO.getLastUpdateDate()));


        if (JOB_STATUS_ORGANIZATION.equals(BusinessTypeCode.getValueByLevel(level.toUpperCase()).value())) {
            argsMap.put("eventName", EVENT_NAME);
        }
        if (JOB_STATUS_SITE.equals(BusinessTypeCode.getValueByLevel(level.toUpperCase()).value())) {
            argsMap.put("eventName", EVENT_NAME_SITE);
        }
        messageSender.setArgs(argsMap);
        return messageSender;
    }

    @Override
    @Async("notify-executor")
    public void sendSagaFailNotice(SagaInstanceDTO instance) {
        //捕获异常，以免影响saga一致性
        try {
            // 构建消息对象
            MessageSender messageSender = new MessageSender();
            // 消息code
            messageSender.setMessageCode(BusinessTypeCode.SAGA_INSTANCE_FAIL.value());
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
            argsMap.put("userName", user.getRealName());
            receiverList.add(receiver);


            messageSender.setReceiverAddressList(receiverList);
            messageSender.setArgs(argsMap);

            messageClient.async().sendMessage(messageSender);
        } catch (Exception e) {
            LOGGER.error("saga instance fail send notice fail", e);
        }

    }

    @Override
    @Async("notify-executor")
    public void sendSagaFailNoticeForTenant(SagaInstanceDTO instance) {
        try {
            if (!instance.getLevel().equals(ResourceLevel.SITE.value())) {
                MessageSender messageSender = new MessageSender();
                messageSender.setMessageCode(BusinessTypeCode.SAGA_INSTANCE_FAIL_ORG.value());
                messageSender.setTenantId(0L);
                Map<String, String> argsMap = new HashMap<>();
                argsMap.put("sagaInstanceId", instance.getId().toString());
                argsMap.put("sagaCode", instance.getSagaCode());
                argsMap.put("level", instance.getLevel());
                messageSender.setArgs(argsMap);
                Map<String, Object> objectMap = new HashMap<>();
                Long tenantId;
                if (instance.getLevel().equals(ResourceLevel.ORGANIZATION.value())) {
                    tenantId = instance.getSourceId();
                } else {
                    ProjectDTO projectDTO = iamFeignClient.queryProject(instance.getSourceId()).getBody();
                    if (projectDTO == null) {
                        return;
                    }
                    tenantId = projectDTO.getOrganizationId();
                }
                objectMap.put(MessageAdditionalType.PARAM_TENANT_ID.getTypeName(), tenantId);

                messageSender.setAdditionalInformation(objectMap);
                messageClient.async().sendMessage(messageSender);
            }
        } catch (Exception e) {
            LOGGER.error("saga instance fail send notice fail for tenant", e);
        }

    }

    @Override
    @Async("notify-executor")
    public void sendSagaFailNoticeForVindicator(SagaInstanceDTO instance) {
        try {
            if (mapEvent.containsKey(instance.getSagaCode())) {
                Map<String, String> argsMap = new HashMap<>();
                argsMap.put("instanceId", instance.getId().toString());
                switch (instance.getSagaCode()) {
                    case IAM_CREATE_ORG_USER:
                    case IAM_CREATE_USER:
                    case IAM_CREATE_PROJECT:
                        Organization organization = baseServiceClientOperator.queryTenantById(instance.getSourceId());
                        argsMap.put("organizationName", organization.getName());
                        break;
                    default:
                }
                MessageSender messageSender = new MessageSender();
                messageSender.setArgs(argsMap);
                messageSender.setMessageCode(mapEvent.get(instance.getSagaCode()));
                messageSender.setTenantId(0L);
                messageClient.async().sendMessage(messageSender);
            }
        } catch (Exception e) {
            LOGGER.error("saga instance fail send notice fail for vindicator create event", e);
        }

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
        // 平台管理员 创建者 指定用户
        Set<Long> userIds = new HashSet<>();
        for (QuartzTaskMemberDTO notifyMember : notifyMembers) {
            //指定用户或者创建者
            if (MemberType.ASSIGNER.value().equals(notifyMember.getMemberType())
                    || MemberType.CREATOR.value().equals(notifyMember.getMemberType())) {
                userIds.add(notifyMember.getMemberId());
            }
            //平台管理员
            if (MemberType.ROLE.value().equals(notifyMember.getMemberType())) {
                users.addAll(getAdministratorUsers(level, sourceId, notifyMember.getMemberId()));
            }
        }
        if (!CollectionUtils.isEmpty(userIds)) {
            List<User> userList = baseServiceClientOperator.getUserByIds(userIds.toArray(new Long[userIds.size()]));
            userList.forEach(user -> {
                UserDTO userDTO = new UserDTO();
                BeanUtils.copyProperties(user, userDTO);
                users.add(userDTO);
            });
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
        try {
            //feign查询负责人及其组织
            RegistrantInfo registrantInfo = iamFeignClient.queryRegistrantAndAdminId(sagaInstance.getRefId()).getBody();
            LOGGER.info("register failed,ref id:{},registrant info：{}", sagaInstance.getRefId(), registrantInfo);

            // 构建消息对象
            MessageSender messageSender = new MessageSender();
            // 消息code
            messageSender.setMessageCode(BusinessTypeCode.REGISTERORGANIZATION_ABNORMAL.value());
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
        } catch (Exception e) {
            LOGGER.info("Send register Org Fail msg failed");
        }

    }

}
