package io.choerodon.asgard.api.eventhandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.choerodon.asgard.api.dto.RegisterInstancePayloadDTO;
import io.choerodon.asgard.api.service.RegisterInstanceService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Component
public class RegisterInstanceListener {

    public static final String REGISTER_TOPIC = "register-server";
    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterInstanceListener.class);
    private static final String STATUS_UP = "UP";
    private static final String STATUS_DOWN = "DOWN";
    private final ObjectMapper mapper = new ObjectMapper();

    private RegisterInstanceService registerInstanceService;


    @Value("${choerodon.asgard.skipService}")
    private String[] skipServices;

    public RegisterInstanceListener(RegisterInstanceService registerInstanceService) {
        this.registerInstanceService = registerInstanceService;
    }

    /**
     * 监听eureka-instance消息队列的新消息处理
     *
     * @param record 消息信息
     */
    @KafkaListener(topics = REGISTER_TOPIC)
    public void handle(ConsumerRecord<byte[], byte[]> record) {
        String message = new String(record.value());
        try {
            LOGGER.info("receive message from register-server, {}", message);
            RegisterInstancePayloadDTO payload = mapper.readValue(message, RegisterInstancePayloadDTO.class);
            boolean isSkipService =
                    Arrays.stream(skipServices).anyMatch(t -> t.equals(payload.getAppName()));
            if (isSkipService) {
                LOGGER.info("skip message that is skipServices, {}", payload);
                return;
            }
            Observable.just(payload)
                    .delay(2, TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.io())
                    .subscribe((RegisterInstancePayloadDTO payloadDTO) -> {
                        try {
                            if (STATUS_UP.equals(payload.getStatus())) {
                                registerInstanceService.msgConsumer(payload, true);
                            } else if (STATUS_DOWN.equals(payload.getStatus())) {
                                registerInstanceService.msgConsumer(payload, false);
                            }
                        } catch (Exception e) {
                            LOGGER.warn("error happened when registerInstanceService.msgConsumer, {} cause {}", message, e.getCause());
                        }
                    });
        } catch (Exception e) {
            LOGGER.warn("error happened when handle message， {} cause {}", message, e.getCause());
        }
    }


}
