package io.choerodon.asgard;

import io.choerodon.resource.annoation.EnableChoerodonResourceServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@EnableEurekaClient
@EnableChoerodonResourceServer
public class AsgardApplication {

    public static void main(String[] args) {
        SpringApplication.run(AsgardApplication.class, args);
    }

}

