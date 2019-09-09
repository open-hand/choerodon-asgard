package io.choerodon.asgard.api.vo;

import io.swagger.annotations.ApiModelProperty;
/**
 * @author tom
 * @since 2019/9/9
 */
public class ScheduleTaskSiteSearchVO {

    @ApiModelProperty(value = "状态")
    private String status;

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "模糊查询数组")
    private String[] params;


    public ScheduleTaskSiteSearchVO() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String[] getParams() {
        return params;
    }

    public void setParams(String[] params) {
        this.params = params;
    }
}
