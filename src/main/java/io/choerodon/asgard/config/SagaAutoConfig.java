package io.choerodon.asgard.config;

import io.choerodon.asgard.infra.utils.JsonDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class SagaAutoConfig {

    @Bean(name = "saga")
    public ForkJoinPool forkJoinPool() {
        return new ForkJoinPool();
    }

    @Bean(name = "sagaBackCheckScheduledService")
    public ScheduledExecutorService sagaScheduledExecutorService() {
        return Executors.newScheduledThreadPool(1);
    }

    @Bean
    public JsonDecoder jsonDecoder() {
        return new JsonDecoder();
    }

}
