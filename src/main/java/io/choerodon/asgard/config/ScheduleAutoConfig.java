package io.choerodon.asgard.config;

import org.quartz.Scheduler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Properties;

@Configuration
@EnableConfigurationProperties(QuartzProperties.class)
@ConditionalOnClass({Scheduler.class, SchedulerFactoryBean.class,
        PlatformTransactionManager.class})
@AutoConfigureAfter({DataSourceAutoConfiguration.class})
public class ScheduleAutoConfig {

    private QuartzProperties properties;

    private ApplicationContext applicationContext;

    private DataSource dataSource;

    private ObjectProvider<PlatformTransactionManager> transactionManager;

    public ScheduleAutoConfig(QuartzProperties properties, ApplicationContext applicationContext,
                              DataSource dataSource, ObjectProvider<PlatformTransactionManager> transactionManager) {
        this.properties = properties;
        this.applicationContext = applicationContext;
        this.dataSource = dataSource;
        this.transactionManager = transactionManager;
    }

    @Bean
    @ConditionalOnMissingBean
    public SchedulerFactoryBean schedulerFactoryBean() {
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        schedulerFactoryBean.setJobFactory(new AutowireCapableBeanJobFactory(
                this.applicationContext.getAutowireCapableBeanFactory()));
        schedulerFactoryBean.setBeanName(this.properties.getSchedulerName());
        schedulerFactoryBean.setAutoStartup(this.properties.isAutoStartup());
        schedulerFactoryBean
                .setStartupDelay((int) this.properties.getStartupDelay().getSeconds());
        schedulerFactoryBean.setWaitForJobsToCompleteOnShutdown(
                this.properties.isWaitForJobsToCompleteOnShutdown());
        schedulerFactoryBean
                .setOverwriteExistingJobs(this.properties.isOverwriteExistingJobs());
        if (!this.properties.getProperties().isEmpty()) {
            schedulerFactoryBean
                    .setQuartzProperties(asProperties(this.properties.getProperties()));
        }
        schedulerFactoryBean.setDataSource(dataSource);
        PlatformTransactionManager txManager = transactionManager.getIfUnique();
        if (txManager != null) {
            schedulerFactoryBean.setTransactionManager(txManager);
        }
        return schedulerFactoryBean;
    }

    private Properties asProperties(Map<String, String> source) {
        Properties myProperties = new Properties();
        myProperties.putAll(source);
        return myProperties;
    }

    @Bean(name = "Scheduler")
    public Scheduler scheduler() {
        return schedulerFactoryBean().getScheduler();
    }

}
