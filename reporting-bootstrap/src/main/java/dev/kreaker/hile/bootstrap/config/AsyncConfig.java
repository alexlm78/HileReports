package dev.kreaker.hile.bootstrap.config;

import java.util.Map;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {

  @Bean("exportTaskExecutor")
  TaskExecutor exportTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(5);
    executor.setQueueCapacity(50);
    executor.setThreadNamePrefix("export-");
    executor.setTaskDecorator(
        runnable -> {
          Map<String, String> mdcContext = MDC.getCopyOfContextMap();
          return () -> {
            try {
              if (mdcContext != null) MDC.setContextMap(mdcContext);
              runnable.run();
            } finally {
              MDC.clear();
            }
          };
        });
    executor.initialize();
    return executor;
  }
}
