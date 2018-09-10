package io.choerodon.asgard.infra.utils;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.AbstractRefreshableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

public class SpringApplicationContextHelper implements ApplicationContextAware {

    private static DefaultListableBeanFactory springFactory;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        if (applicationContext instanceof AbstractRefreshableApplicationContext) {
            AbstractRefreshableApplicationContext springContext =
                    (AbstractRefreshableApplicationContext) applicationContext;
            setSpringFactory((DefaultListableBeanFactory) springContext.getBeanFactory());
        } else if (applicationContext instanceof GenericApplicationContext) {
            GenericApplicationContext springContext = (GenericApplicationContext) applicationContext;
            setSpringFactory(springContext.getDefaultListableBeanFactory());
        }
    }

    private static void setSpringFactory(DefaultListableBeanFactory springFactory) {
        SpringApplicationContextHelper.springFactory = springFactory;
    }

    public static DefaultListableBeanFactory getSpringFactory() {
        return springFactory;
    }

}
