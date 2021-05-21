package io.choerodon.asgard.infra.enums;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;

/**
 * @author dengyouquan
 **/
public enum BusinessTypeCode {
    JOB_STATUS_SITE("JOBSTATUSSITE"),
    JOB_STATUS_ORGANIZATION("JOBSTATUSORGANIZATION"),
    REGISTERORGANIZATION_ABNORMAL("REGISTERORGANIZATION-ABNORMAL"),
    SAGA_INSTANCE_FAIL("SAGAINSTANCEFAIL"),
    /**
     * 组织层事务失败
     */
    SAGA_INSTANCE_FAIL_ORG("SAGAINSTANCEFAILORG"),
    VINDICATOR_CREATE_USER_FAILED("VINDICATOR_USER_FAILED"),
    VINDICATOR_CREATE_ORGANIZATION_FAILED("VINDICATOR_ORGANIZATION_FAILED"),
    VINDICATOR_CREATE_PROJECT_FAILED("VINDICATOR_PROJECT_FAILED"),
    JOB_STATUS_PROJECT("JOBSTATUSPROJECT");
    private String value;

    public String value() {
        return value;
    }

    BusinessTypeCode(String value) {
        this.value = value;
    }

    public static BusinessTypeCode getValueByLevel(String level) {
        if (ResourceLevel.SITE == ResourceLevel.valueOf(level)) {
            return JOB_STATUS_SITE;
        }
        if (ResourceLevel.ORGANIZATION == ResourceLevel.valueOf(level)) {
            return JOB_STATUS_ORGANIZATION;
        }
        if (ResourceLevel.PROJECT == ResourceLevel.valueOf(level)) {
            return JOB_STATUS_PROJECT;
        }
        throw new CommonException("error.level.mismatch");
    }
}
