package dev.kreaker.hile.application.port.out;

public interface MetricsPort {

  void recordExecution(String status, long durationMs, long rowCount);

  void recordExport(String format, String status, long durationMs);

  MetricsPort NOOP =
      new MetricsPort() {
        public void recordExecution(String status, long durationMs, long rowCount) {}

        public void recordExport(String format, String status, long durationMs) {}
      };
}
