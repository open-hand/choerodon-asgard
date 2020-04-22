package io.choerodon.asgard.api.vo;

import java.util.Date;

public class SagaInstanceFailureVO {
    private Date creationDate;
    private Long failureCount;
    private Double percentage;
    private Long totalCount;

    public SagaInstanceFailureVO() {
    }

    public SagaInstanceFailureVO(Date creationDate, Long failureCount, Double percentage, Long totalCount) {
        this.creationDate = creationDate;
        this.failureCount = failureCount;
        this.percentage = percentage;
        this.totalCount = totalCount;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public Long getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(Long failureCount) {
        this.failureCount = failureCount;
    }
}
