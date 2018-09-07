package io.choerodon.asgard.domain;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@ModifyAudit
@VersionAudit
@Table(name = "ASGARD_QUARTZ_TASK_INSTANCE")
public class QuartzTasKInstance extends AuditDomain {

    @Id
    @GeneratedValue
    private Long id;

    private Long taskId;

    private Date plannedStartTime;

    private Date actualStartTime;

    private Date actualLastTime;

    private Date plannedNextTime;

    private String exceptionMessage;

    private Integer retriedCount;

    private Integer maxRetryCount;

    private String instanceLock;

    private String status;

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

    @Override
    public String toString() {
        return "QuartzTasKInstance{" +
                "id=" + id +
                ", taskId=" + taskId +
                ", plannedStartTime=" + plannedStartTime +
                ", actualStartTime=" + actualStartTime +
                ", actualLastTime=" + actualLastTime +
                ", plannedNextTime=" + plannedNextTime +
                ", exceptionMessage='" + exceptionMessage + '\'' +
                ", retriedCount=" + retriedCount +
                ", maxRetryCount=" + maxRetryCount +
                ", instanceLock='" + instanceLock + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
