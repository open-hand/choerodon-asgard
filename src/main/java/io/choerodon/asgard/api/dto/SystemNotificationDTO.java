package io.choerodon.asgard.api.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;

public class SystemNotificationDTO {
    @ApiModelProperty(value = "定时任务Id")
    private Long taskId;

    @ApiModelProperty(value = "公告内容")
    private String content;

    @ApiModelProperty(value = "公告状态")
    private String status;

    @ApiModelProperty(value = "发送时间(开始放送时间)")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date sendTime;

    @ApiModelProperty(value = "乐观锁版本号")
    private Long objectVersionNumber;

    public enum NotificationStatus {
        COMPLETED("COMPLETED"),
        SENDING("SENDING"),
        WAITING("WAITING"),
        FAILED("FAILED");

        private final String value;

        private NotificationStatus(String value) {
            this.value = value;
        }

        public String value() {
            return this.value;
        }
    }

    public SystemNotificationDTO(Long taskId, String content, Date sendTime, String status) {
        this.taskId = taskId;
        this.content = content;
        this.sendTime = sendTime;
        this.status = status;
    }

    public SystemNotificationDTO() {
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }
}
