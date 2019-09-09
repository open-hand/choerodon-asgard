package io.choerodon.asgard.api.vo;

import io.swagger.annotations.ApiModelProperty;
/**
 * @author tom
 * @since 2019/9/9 16:56
 */
public class SagaTaskInstanceSearchVO {

    @ApiModelProperty(value = "所属sagatask实例")
    private String taskInstanceCode;

    @ApiModelProperty(value = "所属saga实例")
    private String sagaInstanceCode;

    @ApiModelProperty(value = "状态")
    private String status;

    @ApiModelProperty(value = "参数数组")
    private String[] params;

    public SagaTaskInstanceSearchVO(String taskInstanceCode, String sagaInstanceCode, String status, String[] params) {
        this.taskInstanceCode = taskInstanceCode;
        this.sagaInstanceCode = sagaInstanceCode;
        this.status = status;
        this.params = params;
    }

    public SagaTaskInstanceSearchVO() {
    }

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
