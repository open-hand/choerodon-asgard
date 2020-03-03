package io.choerodon.asgard;

import io.choerodon.asgard.app.eventhandler.SagaInstanceEventPublisher;
import io.choerodon.asgard.app.eventhandler.SagaInstanceHandler;
import io.choerodon.asgard.infra.config.AsgardProperties;
import io.choerodon.asgard.infra.utils.SpringApplicationContextHelper;
import io.choerodon.eureka.event.EurekaEventHandler;
import io.choerodon.resource.annoation.EnableChoerodonResourceServer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@SpringBootApplication
@EnableFeignClients
@EnableEurekaClient
@EnableChoerodonResourceServer
@EnableConfigurationProperties(AsgardProperties.class)
@EnableAsync
public class AsgardApplication {

    public static void main(String[] args) {
        EurekaEventHandler.getInstance().init();
        SpringApplication.run(AsgardApplication.class, args);
    }

    @Bean
    public SpringApplicationContextHelper applicationContextHelper() {
        return new SpringApplicationContextHelper();
    }

    @Bean
    @Qualifier("notify-executor")
    public AsyncTaskExecutor asyncSendNoticeExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("notify-executor");
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(3);
        executor.setQueueCapacity(100);
        return executor;
    }
    

    /**
     * 实现Asgard Client长连接查询可执行的任务，当有任务时直接返回，没有任务，则等待，直到超时或接收到新任务的通知消息。
     * 新任务的通知消息通过redis来发送和接收
     * 发送消息的点有：
     * 1，SagaTaskInstance创建，Retry,标记为等待执行
     * 2，QuartzTaskInstance创建
     */
    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
                                            MessageListenerAdapter sagaInstanceAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(sagaInstanceAdapter, new PatternTopic(SagaInstanceEventPublisher.SAGA_INSTANCE_TOPIC));
        return container;
    }

    @Bean
    SagaInstanceHandler sagaInstanceHandler() {
        return new SagaInstanceHandler();
    }

    @Bean
    MessageListenerAdapter sagaInstanceAdapter(SagaInstanceHandler sagaInstanceHandler) {
        MessageListenerAdapter sagaInstanceAdapter = new MessageListenerAdapter(sagaInstanceHandler, "onMessage");
        StringRedisSerializer stringRedisSerializer = this.stringRedisSerializer();
        sagaInstanceAdapter.setSerializer(stringRedisSerializer);
        return sagaInstanceAdapter;
    }


    private StringRedisSerializer stringRedisSerializer() {
        return new StringRedisSerializer();
    }


}

