package io.choerodon.asgard.api.vo;

import java.util.Date;

public class SagaInstanceFailureVO {
    private Date creationDate;
    private Long count;

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
