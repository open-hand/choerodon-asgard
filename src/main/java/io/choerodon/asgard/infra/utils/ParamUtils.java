package io.choerodon.asgard.infra.utils;

/**
 * @author lrc
 * @since 2019/9/9
 */
public class ParamUtils {
    public static final Integer VIEWID_DIGIT = 6;

    private ParamUtils() {
    }

    public static String arrToStr(String[] params) {
        if (params == null) {
            return null;
        }
        if (params.length > 1) {
            StringBuilder sb = new StringBuilder();
            for (String ele : params) {
                sb.append(ele).append(",");
            }
            return sb.toString();
        }
        String param = null;
        if (params.length == 1) {
            param = params[0];
        }
        return param;
    }

    public static String handId(Long handId) {
        String id = String.valueOf(handId);
        if (id.length() >= VIEWID_DIGIT) {
            return (org.apache.commons.lang3.StringUtils.reverse(org.apache.commons.lang3.StringUtils.substring(org.apache.commons.lang3.StringUtils.reverse(id), 0, VIEWID_DIGIT)));
        }
        return id;
    }
}
