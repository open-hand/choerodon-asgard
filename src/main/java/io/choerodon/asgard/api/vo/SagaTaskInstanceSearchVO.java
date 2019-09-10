package io.choerodon.asgard.api.vo;

import io.swagger.annotations.ApiModelProperty;

/**
 * SagaTask实例查询VO.
 *
 * @author tom
 * @since 2019/9/9
 */
public class SagaTaskInstanceSearchVO {

    @ApiModelProperty(value = "SagaTask实例编码")
    private String taskInstanceCode;

    @ApiModelProperty(value = "所属Saga实例编码")
    private String sagaInstanceCode;

    @ApiModelProperty(value = "状态")
    private String status;

    @ApiModelProperty(value = "模糊查询数组")
    private String[] params;

    public String getTaskInstanceCode() {
        return taskInstanceCode;
    }

    public void setTaskInstanceCode(String taskInstanceCode) {
        this.taskInstanceCode = taskInstanceCode;
    }

    public String getSagaInstanceCode() {
        return sagaInstanceCode;
    }

    public void setSagaInstanceCode(String sagaInstanceCode) {
        this.sagaInstanceCode = sagaInstanceCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String[] getParams() {
        return params;
    }

    public void setParams(String[] params) {
        this.params = params;
    }
}
