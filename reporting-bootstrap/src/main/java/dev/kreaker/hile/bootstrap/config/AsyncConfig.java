package dev.kreaker.hile.bootstrap.config;

import java.util.Map;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
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

  @Value("${hile.reports.export.async.core-pool-size:2}")
  private int corePoolSize;

  @Value("${hile.reports.export.async.max-pool-size:5}")
  private int maxPoolSize;

  @Value("${hile.reports.export.async.queue-capacity:50}")
  private int queueCapacity;

  @Bean("exportTaskExecutor")
  TaskExecutor exportTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(corePoolSize);
    executor.setMaxPoolSize(maxPoolSize);
    executor.setQueueCapacity(queueCapacity);
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
