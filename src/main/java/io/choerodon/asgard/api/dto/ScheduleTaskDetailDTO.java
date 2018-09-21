package io.choerodon.asgard.api.dto;

import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiModelProperty;

import io.choerodon.asgard.domain.QuartzTaskDetail;
import io.choerodon.core.exception.CommonException;

public class ScheduleTaskDetailDTO {
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

    public ScheduleTaskDetailDTO() {
    }

    public ScheduleTaskDetailDTO(final QuartzTaskDetail quartzTaskDetail, final ObjectMapper objectMapper, final Date lastExecTime, final Date nextExecTime) {
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
            Map<String, Object> paramsMap = objectMapper.readValue(quartzTaskDetail.getParams(), new TypeReference<Map<String, String>>() {
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
}
