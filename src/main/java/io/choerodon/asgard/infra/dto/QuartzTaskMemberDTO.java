package io.choerodon.asgard.infra.dto;


import io.choerodon.mybatis.domain.AuditDomain;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

/**
 * @author dengyouquan
 **/
@Table(name = "ASGARD_QUARTZ_TASK_MEMBER")
public class QuartzTaskMemberDTO extends AuditDomain {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long taskId;
    private String memberType;
    private Long memberId;

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

    public String getMemberType() {
        return memberType;
    }

    public void setMemberType(String memberType) {
        this.memberType = memberType;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    @Override
    public String toString() {
        return "QuartzTaskMemberDTO{" +
                "id=" + id +
                ", taskId=" + taskId +
                ", memberType='" + memberType + '\'' +
                ", memberId=" + memberId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuartzTaskMemberDTO that = (QuartzTaskMemberDTO) o;
        return Objects.equals(taskId, that.taskId) &&
                Objects.equals(memberType, that.memberType) &&
                Objects.equals(memberId, that.memberId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId, memberType, memberId);
    }
}
