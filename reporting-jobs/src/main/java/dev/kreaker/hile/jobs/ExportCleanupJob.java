package dev.kreaker.hile.jobs;

import java.time.Instant;

public class ExportCleanupJob {

  public Instant nextPlannedExecution() {
    return Instant.now().plusSeconds(3600);
  }
}
