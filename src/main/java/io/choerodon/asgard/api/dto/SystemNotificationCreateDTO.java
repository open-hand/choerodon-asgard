package io.choerodon.asgard.api.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * @author Eugen
 */
public class SystemNotificationCreateDTO {
    @ApiModelProperty(value = "公告内容/必填")
    @NotEmpty(message = "error.system.notification.content.empty")
    private String content;

    @ApiModelProperty(value = "发送时间：不填时默认为当前时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }
}
