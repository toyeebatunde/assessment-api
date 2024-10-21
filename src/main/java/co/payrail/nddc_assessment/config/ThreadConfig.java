package co.payrail.nddc_assessment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class ThreadConfig {
    public static ThreadPoolTaskExecutor getExecutor(){
        return taskExecutor();
    }


    @Bean
    @Primary
    private static ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); // Set the initial number of threads
        executor.setMaxPoolSize(10); // Set the maximum number of threads
        executor.setQueueCapacity(10); // Set the capacity of the task queue
        executor.setThreadNamePrefix("AuthServiceThread-"); // Set a prefix for thread names
        executor.initialize();
        return executor;
    }
}
