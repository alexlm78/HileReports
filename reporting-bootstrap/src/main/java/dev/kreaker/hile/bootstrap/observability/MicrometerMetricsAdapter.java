package dev.kreaker.hile.bootstrap.observability;

import dev.kreaker.hile.application.port.out.MetricsPort;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

@Component
public class MicrometerMetricsAdapter implements MetricsPort {

  private final MeterRegistry meterRegistry;

  public MicrometerMetricsAdapter(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
  }

  @Override
  public void recordExecution(String status, long durationMs, long rowCount) {
    Counter.builder("hile.reports.execution.total")
        .tag("status", status)
        .register(meterRegistry)
        .increment();
    Timer.builder("hile.reports.execution.duration")
        .tag("status", status)
        .register(meterRegistry)
        .record(durationMs, TimeUnit.MILLISECONDS);
    if (rowCount >= 0) {
      meterRegistry.summary("hile.reports.execution.rows", "status", status).record(rowCount);
    }
  }

  @Override
  public void recordExport(String format, String status, long durationMs) {
    Counter.builder("hile.reports.export.total")
        .tag("format", format)
        .tag("status", status)
        .register(meterRegistry)
        .increment();
    Timer.builder("hile.reports.export.duration")
        .tag("format", format)
        .tag("status", status)
        .register(meterRegistry)
        .record(durationMs, TimeUnit.MILLISECONDS);
  }
}
