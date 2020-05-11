package cn.lip.mybatis.application;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

public class MyApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        System.out.println("=========MyApplicationContextInitializer===========");
        System.out.println(applicationContext.getBeanDefinitionCount());
    }
}