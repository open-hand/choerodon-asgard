package io.choerodon.asgard.infra.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.Decoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;

public class JsonDecoder implements Decoder {
    private static final String TYPE_NAME_PREFIX = "class ";
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonDecoder.class);

    @Override
    public Object decode(Response response, Type type) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Class<?> clazz = null;
        try {
            clazz = getClass(type);
        } catch (ClassNotFoundException e) {
            LOGGER.info("decode getClass error:{}", e);
        }
        if (clazz == null) {
            return null;
        } else {
            return objectMapper.readValue(response.body().asReader(), clazz);
        }
    }

    private static String getClassName(Type type) {
        if (type == null) {
            return "";
        }
        String className = type.toString();
        if (className.startsWith(TYPE_NAME_PREFIX)) {
            className = className.substring(TYPE_NAME_PREFIX.length());
        }
        return className;
    }

    private static Class getClass(Type type)
            throws ClassNotFoundException {
        String className = getClassName(type);
        if (className == null || className.isEmpty()) {
            return null;
        }
        return Class.forName(className);
    }
}

