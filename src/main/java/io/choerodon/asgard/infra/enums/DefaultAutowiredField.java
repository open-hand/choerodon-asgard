package io.choerodon.asgard.infra.enums;

/**
 * @author dengyouquan
 **/
public class DefaultAutowiredField {
    /**
     * 组织层定时任务默认自动注入参数
     */
    public static final String ORGANIZATION_ID = "organizationId";
    public static final String ORGANIZATION_NAME = "organizationName";
    public static final String ORGANIZATION_CODE = "organizationCode";

    /**
     * 项目层定时任务默认自动注入参数
     */
    public static final String PROJECT_ID = "projectId";
    public static final String PROJECT_NAME = "projectName";
    public static final String PROJECT_CODE = "projectCode";

    public static String[] organizationDefaultField() {
        return new String[]{ORGANIZATION_ID, ORGANIZATION_NAME, ORGANIZATION_CODE};
    }

    public static String[] projectDefaultField() {
        return new String[]{PROJECT_ID, PROJECT_NAME, PROJECT_CODE};
    }
}
