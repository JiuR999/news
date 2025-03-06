package com.swust.wemedia.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ThreadPoolConfig {
    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);      // 核心线程数
        executor.setMaxPoolSize(8);       // 最大线程数
        executor.setQueueCapacity(100);   // 队列容量
        executor.setThreadNamePrefix("FileUpload-");
        executor.initialize();
        return executor;
    }

}
