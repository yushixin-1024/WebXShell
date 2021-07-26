package org.point.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 项目配置类
 */
@Slf4j
@Configuration
@EnableAsync
@EnableScheduling
public class ShellConfig {

    /**
     * 设置事件监听线程池
     */
    @Bean
    public SimpleApplicationEventMulticaster applicationEventMulticaster(BeanFactory factory, ThreadPoolTaskExecutor threadPoolTaskExecutor) {
        SimpleApplicationEventMulticaster multicaster = new SimpleApplicationEventMulticaster(factory);
        multicaster.setTaskExecutor(threadPoolTaskExecutor);
        multicaster.setErrorHandler(t -> log.error("事件处理异常", t));
        return multicaster;
    }

    /**
     * 自定义线程池配置
     */
    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("线程池分配-");
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(32);
        executor.setQueueCapacity(32);
        executor.setKeepAliveSeconds(60);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.initialize();
        return executor;
    }
}