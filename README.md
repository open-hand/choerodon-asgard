# asgard-service

Choerodon Asgard Service 是一个任务调度服务，通过`saga` 实现微服务之间的数据一致性。

## 特征

实现数据最终一致性

## 服务配置

- `application.yml`

  ```yaml
    spring:
      datasource:
        url: jdbc:mysql://localhost:3306/asgard_service?useUnicode=true&characterEncoding=utf-8&useSSL=false&useInformationSchema=true&remarks=true
        username: choerodon
        password: 123456
      # 配置redis，作为asgard服务的通知消息队列
      redis:
        host: localhost
        port: 6379
        database: 7
    eureka:
      instance:
        preferIpAddress: true
        leaseRenewalIntervalInSeconds: 10
        leaseExpirationDurationInSeconds: 30
        metadata-map:
          VERSION: v1
      client:
        serviceUrl:
          defaultZone: http://localhost:8000/eureka/
        registryFetchIntervalSeconds: 10
    hystrix:
      command:
        default:
          execution:
            isolation:
              thread:
                timeoutInMilliseconds: 20000
    ribbon:
      ReadTimeout: 5000
      ConnectTimeout: 5000
    notify-service:
      ribbon:
        MaxAutoRetries: 0
        MaxAutoRetriesNextServer: 0
    mybatis:
      mapperLocations: classpath*:/mapper/*.xml
      configuration: # 数据库下划线转驼峰配置
        mapUnderscoreToCamelCase: true
    choerodon:
      saga:
        consumer:
          enabled: true # 启动消费端
          thread-num: 2 # saga消息消费线程池大小
          max-poll-size: 200 # 每次拉取消息最大数量
          poll-interval-ms: 1000 # 拉取间隔，默认1000毫秒
      asgard:
        quartz:
          auto-startup: true
          overwrite-existing-jobs: true
          properties:
            org.quartz.jobStore.tablePrefix: QRTZ_
            org.quartz.jobStore.driverDelegateClass: org.quartz.impl.jdbcjobstore.StdJDBCDelegate
            org.quartz.jobStore.class: org.quartz.impl.jdbcjobstore.JobStoreTX
            org.quartz.jobStore.isClustered: true
            org.quartz.jobStore.misfireThreshold: 25000
            org.quartz.scheduler.makeSchedulerThreadDaemon: true
        saga:
          back-check-interval-ms: 1000 # 每隔多久回查(毫秒)
          un-confirmed-timeout-seconds: 300 # 没查超过多久仍未确认的消息(秒)
      eureka:
        event:
          max-cache-size: 300 # 存储的最大失败数量
          retry-time: 5 # 自动重试次数
          retry-interval: 3 # 自动重试间隔(秒)
          skip-services: config**, **register-server, **gateway**, zipkin**, hystrix**, oauth**
      schedule:
        consumer:
          enabled: true
          poll-interval-ms: 10000
    db:
      type: mysql
  ```

- `bootstrap.yml`

  ```yaml
  server:
    port: 18080
  spring:
    application:
      name: asgard-service
    cloud:
      config:
        failFast: true
        retry:
          maxAttempts: 6
          multiplier: 1.5
          maxInterval: 2000
        uri: localhost:8010
        enabled: false
    mvc:
      static-path-pattern: /**
    resources:
      static-locations: classpath:/static,classpath:/public,classpath:/resources,classpath:/META-INF/resources,file:/dist
  management:
    endpoint:
      health:
        show-details: ALWAYS
    server:
      port: 18081
    endpoints:
      web:
        exposure:
          include: '*'
  ```

## 环境需求

- mysql 5.6+
- redis 3.0+
- 该项目是一个 Eureka Client 项目，启动后需要注册到 `EurekaServer`，本地环境需要 `eureka-server`，线上环境需要使用 `go-register-server`

## 安装和启动步骤

- 运行 `eureka-server`，[代码库地址](https://code.choerodon.com.cn/choerodon-framework/eureka-server.git)。

- 拉取当前项目到本地，执行如下命令：

  ```sh
   git clone https://code.choerodon.com.cn/choerodon-framework/asgard-service.git
  ```

- 创建数据库，本地创建 `asgard_service` 数据库和默认用户，示例如下：

  ```sql
  CREATE USER 'choerodon'@'%' IDENTIFIED BY "123456";
  CREATE DATABASE asgard_service DEFAULT CHARACTER SET utf8;
  GRANT ALL PRIVILEGES ON asgard_service.* TO choerodon@'%';
  FLUSH PRIVILEGES;
  ```

- 初始化 `asgard_service` 数据库，运行项目根目录下的 `init-local-database.sh`，该脚本默认初始化数据库的地址为 `localhost`，若有变更需要修改脚本文件

  ```sh
  sh init-local-database.sh
  ```

- 本地启动 redis-server

- 启动项目，项目根目录下执行如下命令：

  ```sh
   mvn spring-boot:run
  ```
  
## 更新日志

* [更新日志](./CHANGELOG.zh-CN.md)


## 如何参与

欢迎参与我们的项目，了解更多有关如何[参与贡献](https://github.com/choerodon/choerodon/blob/master/CONTRIBUTING.md)的信息。

