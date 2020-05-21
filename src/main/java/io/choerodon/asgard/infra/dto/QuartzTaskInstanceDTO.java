package io.choerodon.asgard.infra.dto;

import io.choerodon.mybatis.domain.AuditDomain;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;


@Table(name = "ASGARD_QUARTZ_TASK_INSTANCE")
public class QuartzTaskInstanceDTO extends AuditDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long taskId;

    private String taskName;

    private Date plannedStartTime;

    private Date actualStartTime;

    private Date actualLastTime;

    private Date plannedNextTime;

    private String exceptionMessage;

    private Integer retriedCount;

    private Integer maxRetryCount;

    private String instanceLock;

    private String status;

    private String executeParams;

    private String executeMethod;

    private String executeResult;

    @Column(name = "FD_LEVEL")
    private String level;

    private Long sourceId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Date getPlannedStartTime() {
        return plannedStartTime;
    }

    public void setPlannedStartTime(Date plannedStartTime) {
        this.plannedStartTime = plannedStartTime;
    }

    public Date getActualStartTime() {
        return actualStartTime;
    }

    public void setActualStartTime(Date actualStartTime) {
        this.actualStartTime = actualStartTime;
    }

    public Date getPlannedNextTime() {
        return plannedNextTime;
    }

    public void setPlannedNextTime(Date plannedNextTime) {
        this.plannedNextTime = plannedNextTime;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    public Integer getRetriedCount() {
        return retriedCount;
    }

    public void setRetriedCount(Integer retriedCount) {
        this.retriedCount = retriedCount;
    }

    public String getInstanceLock() {
        return instanceLock;
    }

    public void setInstanceLock(String instanceLock) {
        this.instanceLock = instanceLock;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getActualLastTime() {
        return actualLastTime;
    }

    public void setActualLastTime(Date actualLastTime) {
        this.actualLastTime = actualLastTime;
    }

    public Integer getMaxRetryCount() {
        return maxRetryCount;
    }

    public void setMaxRetryCount(Integer maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    public String getExecuteParams() {
        return executeParams;
    }

    public void setExecuteParams(String executeParams) {
        this.executeParams = executeParams;
    }

    public String getExecuteMethod() {
        return executeMethod;
    }

    public void setExecuteMethod(String executeMethod) {
        this.executeMethod = executeMethod;
    }

    public String getExecuteResult() {
        return executeResult;
    }

    public void setExecuteResult(String executeResult) {
        this.executeResult = executeResult;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuartzTaskInstanceDTO that = (QuartzTaskInstanceDTO) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "QuartzTaskInstanceDTO{" +
                "id=" + id +
                ", taskId=" + taskId +
                ", taskName='" + taskName + '\'' +
                ", plannedStartTime=" + plannedStartTime +
                ", actualStartTime=" + actualStartTime +
                ", actualLastTime=" + actualLastTime +
                ", plannedNextTime=" + plannedNextTime +
                ", exceptionMessage='" + exceptionMessage + '\'' +
                ", retriedCount=" + retriedCount +
                ", maxRetryCount=" + maxRetryCount +
                ", instanceLock='" + instanceLock + '\'' +
                ", status='" + status + '\'' +
                ", executeParams='" + executeParams + '\'' +
                ", executeMethod='" + executeMethod + '\'' +
                ", executeResult='" + executeResult + '\'' +
                ", level='" + level + '\'' +
                ", sourceId='" + sourceId + '\'' +
                '}';
    }
}
