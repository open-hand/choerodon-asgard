package io.choerodon.asgard.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.choerodon.core.oauth.CustomUserDetails;

import java.util.Objects;

public class PollScheduleTaskInstanceDTO {

    private Long id;

    private String method;

    private String executeParams;

    private String instanceLock;

    private Long objectVersionNumber;

    private CustomUserDetails userDetails;

    @JsonIgnore
    private String userDetailsJson;

    @JsonIgnore
    private String executeStrategy;

    @JsonIgnore
    private Long taskId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getExecuteParams() {
        return executeParams;
    }

    public void setExecuteParams(String executeParams) {
        this.executeParams = executeParams;
    }

    public String getInstanceLock() {
        return instanceLock;
    }

    public void setInstanceLock(String instanceLock) {
        this.instanceLock = instanceLock;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public CustomUserDetails getUserDetails() {
        return userDetails;
    }

    public void setUserDetails(CustomUserDetails userDetails) {
        this.userDetails = userDetails;
    }

    public String getUserDetailsJson() {
        return userDetailsJson;
    }

    public void setUserDetailsJson(String userDetailsJson) {
        this.userDetailsJson = userDetailsJson;
    }

    public String getExecuteStrategy() {
        return executeStrategy;
    }

    public void setExecuteStrategy(String executeStrategy) {
        this.executeStrategy = executeStrategy;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PollScheduleTaskInstanceDTO that = (PollScheduleTaskInstanceDTO) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PollScheduleTaskInstanceDTO{" +
                "id=" + id +
                ", method='" + method + '\'' +
                ", executeParams='" + executeParams + '\'' +
                ", instanceLock='" + instanceLock + '\'' +
                ", objectVersionNumber=" + objectVersionNumber +
                ", userDetails=" + userDetails +
                ", userDetailsJson='" + userDetailsJson + '\'' +
                ", executeStrategy='" + executeStrategy + '\'' +
                ", taskId=" + taskId +
                '}';
    }
}
