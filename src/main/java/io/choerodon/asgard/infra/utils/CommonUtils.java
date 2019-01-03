package io.choerodon.asgard.infra.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CommonUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonUtils.class);

    private CommonUtils() {
    }

    public static String sha256(String str) {
        MessageDigest messageDigest;
        String enCodeStr = "";
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(str.getBytes("UTF-8"));
            enCodeStr = byte2Hex(messageDigest.digest());
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            LOGGER.warn("error.commonUtils.sha256", e);
        }
        return enCodeStr;
    }

    private static String byte2Hex(byte[] bytes) {
        StringBuilder stringBuffer = new StringBuilder();
        String temp;
        for (int i = 0; i < bytes.length; i++) {
            temp = Integer.toHexString(bytes[i] & 0xFF);
            if (temp.length() == 1) {
                stringBuffer.append("0");
            }
            stringBuffer.append(temp);
        }
        return stringBuffer.toString();
    }

    public static String getUserDetailsJson(final ObjectMapper objectMapper) {
        CustomUserDetails userDetails = DetailsHelper.getUserDetails();
        if (userDetails == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(userDetails);
        } catch (JsonProcessingException e) {
            LOGGER.warn("error.commonUtils.getUserDetailsJson", e);
            return null;
        }
    }

    public static CustomUserDetails readJsonAsUserDetails(final ObjectMapper objectMapper, final String json) {
        try {
            return objectMapper.readValue(json, CustomUserDetails.class);
        } catch (IOException e) {
            LOGGER.warn("error.commonUtils.readJsonAsUserDetails", e);
            return null;
        }
    }

}
