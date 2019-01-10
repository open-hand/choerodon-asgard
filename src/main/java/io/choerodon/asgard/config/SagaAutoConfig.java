package io.choerodon.asgard.config;

import feign.Client;
import io.choerodon.asgard.api.service.SagaInstanceService;
import io.choerodon.asgard.api.timer.BackCheckSagaStatusTimer;
import io.choerodon.asgard.api.timer.CleanSagaDataTimer;
import io.choerodon.asgard.infra.mapper.SagaInstanceMapper;
import io.choerodon.asgard.infra.mapper.SagaTaskInstanceMapper;
import io.choerodon.feign.FeignRequestInterceptor;
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

    @Bean(name = "backCheckSagaStatusTimerThread")
    public ScheduledExecutorService backCheckSagaStatusTimerThread() {
        return Executors.newScheduledThreadPool(1);
    }


    @Bean
    public BackCheckSagaStatusTimer backCheckSagaStatusTimer(SagaInstanceMapper instanceMapper,
                                                             AsgardProperties asgardProperties,
                                                             FeignRequestInterceptor feignRequestInterceptor,
                                                             Client client,
                                                             SagaInstanceService sagaInstanceService) {
        return new BackCheckSagaStatusTimer(backCheckSagaStatusTimerThread(), instanceMapper, asgardProperties,
                feignRequestInterceptor, client, sagaInstanceService);
    }

    @Bean
    public CleanSagaDataTimer cleanSagaDataTimer(SagaInstanceMapper instanceMapper,
                                                 SagaTaskInstanceMapper taskInstanceMapper) {
        return new CleanSagaDataTimer(instanceMapper, taskInstanceMapper);
    }


}
