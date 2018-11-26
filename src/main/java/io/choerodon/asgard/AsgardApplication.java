package io.choerodon.asgard;

import io.choerodon.asgard.infra.utils.SpringApplicationContextHelper;
import io.choerodon.eureka.event.EurekaEventHandler;
import io.choerodon.resource.annoation.EnableChoerodonResourceServer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@SpringBootApplication
@EnableFeignClients
@EnableEurekaClient
@EnableChoerodonResourceServer
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
}

