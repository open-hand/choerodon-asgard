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
    JOB_STATUS_PROJECT("JOBSTATUSPROJECT");
    private String value;

    public String value() {
        return value;
    }

    BusinessTypeCode(String value) {
        this.value = value;
    }

    public static BusinessTypeCode getValueByLevel(String level) {
        if (ResourceLevel.SITE.value().equals(level)) {
            return JOB_STATUS_SITE;
        }
        if (ResourceLevel.ORGANIZATION.value().equals(level)) {
            return JOB_STATUS_ORGANIZATION;
        }
        if (ResourceLevel.PROJECT.value().equals(level)) {
            return JOB_STATUS_PROJECT;
        }
        throw new CommonException("error.level.mismatch");
    }
}
