package io.choerodon.asgard.api.vo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.choerodon.core.exception.CommonException;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import java.io.IOException;
import java.util.*;

public class ScheduleTaskDetail {
    @Encrypt
    @ApiModelProperty(value = "主键id")
    private Long id;
    @ApiModelProperty(value = "输入参数的map形式")
    private List<Map<String, Object>> params;

    @ApiModelProperty(value = "任务名称")
    private String name;

    @ApiModelProperty(value = "任务描述")
    private String description;

    @ApiModelProperty(value = "开始时间")
    private Date startTime;

    @ApiModelProperty(value = "结束时间")
    private Date endTime;

    @ApiModelProperty(value = "触发类型。simple-trigger或cron-trigger")
    private String triggerType;

    @ApiModelProperty(value = "simple-trigger的重复次数")
    private Integer simpleRepeatCount;

    @ApiModelProperty(value = "simple-trigger的重复间隔-数值")
    private Long simpleRepeatInterval;

    @ApiModelProperty(value = "simple-trigger的重复间隔-单位")
    private String simpleRepeatIntervalUnit;

    @ApiModelProperty(value = "cron-trigger的cron表达式")
    private String cronExpression;

    @ApiModelProperty(value = "上次执行时间")
    private Date lastExecTime;

    @ApiModelProperty(value = "下次执行时间")
    private Date nextExecTime;

    @ApiModelProperty(value = "服务名")
    private String serviceName;

    @ApiModelProperty(value = "任务类名")
    private String methodCode;

    @ApiModelProperty(value = "方法描述")
    private String methodDescription;

    @ApiModelProperty(value = "通知对象")
    private NotifyUser notifyUser;

    @ApiModelProperty(value = "执行策略")
    private String executeStrategy;

    private Long objectVersionNumber;

    public NotifyUser getNotifyUser() {
        return notifyUser;
    }

    public void setNotifyUser(NotifyUser notifyUser) {
        this.notifyUser = notifyUser;
    }

    public List<Map<String, Object>> getParams() {
        return params;
    }

    public void setParams(List<Map<String, Object>> params) {
        this.params = params;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    public Integer getSimpleRepeatCount() {
        return simpleRepeatCount;
    }

    public void setSimpleRepeatCount(Integer simpleRepeatCount) {
        this.simpleRepeatCount = simpleRepeatCount;
    }

    public Long getSimpleRepeatInterval() {
        return simpleRepeatInterval;
    }

    public void setSimpleRepeatInterval(Long simpleRepeatInterval) {
        this.simpleRepeatInterval = simpleRepeatInterval;
    }

    public String getSimpleRepeatIntervalUnit() {
        return simpleRepeatIntervalUnit;
    }

    public void setSimpleRepeatIntervalUnit(String simpleRepeatIntervalUnit) {
        this.simpleRepeatIntervalUnit = simpleRepeatIntervalUnit;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public Date getLastExecTime() {
        return lastExecTime;
    }

    public void setLastExecTime(Date lastExecTime) {
        this.lastExecTime = lastExecTime;
    }

    public Date getNextExecTime() {
        return nextExecTime;
    }

    public void setNextExecTime(Date nextExecTime) {
        this.nextExecTime = nextExecTime;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getMethodCode() {
        return methodCode;
    }

    public void setMethodCode(String methodCode) {
        this.methodCode = methodCode;
    }

    public String getMethodDescription() {
        return methodDescription;
    }

    public String getExecuteStrategy() {
        return executeStrategy;
    }

    public void setExecuteStrategy(String executeStrategy) {
        this.executeStrategy = executeStrategy;
    }

    public void setMethodDescription(String methodDescription) {
        this.methodDescription = methodDescription;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public ScheduleTaskDetail() {
    }

    public ScheduleTaskDetail(final QuartzTaskDetail quartzTaskDetail, final ObjectMapper objectMapper, final Date lastExecTime, final Date nextExecTime) {
        this.id = quartzTaskDetail.getId();
        this.name = quartzTaskDetail.getName();
        this.description = quartzTaskDetail.getDescription();
        this.startTime = quartzTaskDetail.getStartTime();
        this.endTime = quartzTaskDetail.getEndTime();
        this.triggerType = quartzTaskDetail.getTriggerType();
        this.simpleRepeatCount = quartzTaskDetail.getSimpleRepeatCount();
        this.simpleRepeatInterval = quartzTaskDetail.getSimpleRepeatInterval();
        this.simpleRepeatIntervalUnit = quartzTaskDetail.getSimpleRepeatIntervalUnit();
        this.cronExpression = quartzTaskDetail.getCronExpression();
        this.serviceName = quartzTaskDetail.getServiceName();
        this.methodCode = quartzTaskDetail.getMethodCode();

        this.lastExecTime = lastExecTime;
        this.nextExecTime = nextExecTime;
        this.params = new ArrayList<>();
        try {
            Map<String, Object> paramsMap = objectMapper.readValue(quartzTaskDetail.getParams(), new TypeReference<Map<String, Object>>() {
            });
            paramsMap.forEach((k, v) -> {
                Map<String, Object> param = new HashMap<>();
                param.put("name", k);
                param.put("value", v);
                this.params.add(param);
            });
        } catch (IOException e) {
            throw new CommonException("error.scheduleTaskDetailDTO.jsonIOException", e);
        }

    }

    public static class NotifyUser {
        User creator;
        Boolean administrator;
        List<User> assigner;

        public NotifyUser(User creator, Boolean administrator, List<User> assigner) {
            this.creator = creator;
            this.administrator = administrator;
            this.assigner = assigner;
        }

        public User getCreator() {
            return creator;
        }

        public void setCreator(User creator) {
            this.creator = creator;
        }

        public Boolean getAdministrator() {
            return administrator;
        }

        public void setAdministrator(Boolean administrator) {
            this.administrator = administrator;
        }

        public List<User> getAssigner() {
            return assigner;
        }

        public void setAssigner(List<User> assigner) {
            this.assigner = assigner;
        }
    }

    public static class User {
        private String loginName;
        private String realName;
        private String email;
        private Boolean ldap;
        public User(String loginName, String realName) {
            this.loginName = loginName;
            this.realName = realName;

        }

        public User(String loginName, String realName, Boolean ldap, String email) {
            this.loginName = loginName;
            this.realName = realName;
            this.email = email;
            this.ldap = ldap;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public Boolean getLdap() {
            return ldap;
        }

        public void setLdap(Boolean ldap) {
            this.ldap = ldap;
        }

        public String getLoginName() {
            return loginName;
        }

        public void setLoginName(String loginName) {
            this.loginName = loginName;
        }

        public String getRealName() {
            return realName;
        }

        public void setRealName(String realName) {
            this.realName = realName;
        }
    }
}
