package com.isp392.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2); // Số luồng chạy thường trực
        executor.setMaxPoolSize(5);  // Số luồng tối đa
        executor.setQueueCapacity(100); // Hàng đợi
        executor.setThreadNamePrefix("EmailSender-");
        executor.initialize();
        return executor;
    }
}