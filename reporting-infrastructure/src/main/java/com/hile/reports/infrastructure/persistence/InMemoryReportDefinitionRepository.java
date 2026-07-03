package com.hile.reports.infrastructure.persistence;

import com.hile.reports.application.port.out.ReportDefinitionRepository;
import com.hile.reports.domain.report.ReportDefinition;
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
