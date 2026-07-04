package dev.kreaker.hile.infrastructure.persistence;

import dev.kreaker.hile.application.port.out.ReportDefinitionRepository;
import dev.kreaker.hile.domain.report.ReportDefinition;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryReportDefinitionRepository implements ReportDefinitionRepository {

  private final Map<UUID, ReportDefinition> storage = new ConcurrentHashMap<>();

  @Override
  public ReportDefinition save(ReportDefinition reportDefinition) {
    storage.put(reportDefinition.id(), reportDefinition);
    return reportDefinition;
  }
}
