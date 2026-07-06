package dev.kreaker.hile.infrastructure.persistence;

import dev.kreaker.hile.application.dto.PageResult;
import dev.kreaker.hile.application.port.out.ReportExecutionRepository;
import dev.kreaker.hile.domain.report.ReportExecution;
import dev.kreaker.hile.infrastructure.persistence.entity.ReportExecutionEntity;
import dev.kreaker.hile.infrastructure.persistence.entity.ReportExecutionParameterEntity;
import dev.kreaker.hile.infrastructure.persistence.jpa.ReportExecutionJpaRepository;
import dev.kreaker.hile.infrastructure.persistence.jpa.ReportExecutionParameterJpaRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ReportExecutionRepositoryAdapter implements ReportExecutionRepository {

  private final ReportExecutionJpaRepository executionRepo;
  private final ReportExecutionParameterJpaRepository paramRepo;

  public ReportExecutionRepositoryAdapter(
      ReportExecutionJpaRepository executionRepo, ReportExecutionParameterJpaRepository paramRepo) {
    this.executionRepo = executionRepo;
    this.paramRepo = paramRepo;
  }

  @Override
  @Transactional
  public ReportExecution save(ReportExecution execution, Map<String, String> paramValues) {
    ReportExecutionEntity entity =
        new ReportExecutionEntity(
            execution.id(),
            execution.reportDefinitionId(),
            execution.reportVersionId(),
            execution.requestedBy(),
            execution.requestedAt(),
            execution.status(),
            execution.executionMode(),
            execution.rowCount(),
            execution.durationMs(),
            execution.errorCode(),
            execution.errorMessageSanitized(),
            execution.correlationId());
    executionRepo.save(entity);

    if (paramValues != null) {
      paramValues.forEach(
          (name, value) -> {
            ReportExecutionParameterEntity.Id key =
                new ReportExecutionParameterEntity.Id(execution.id(), name);
            paramRepo.save(new ReportExecutionParameterEntity(key, value));
          });
    }
    return execution;
  }

  @Override
  public Optional<ReportExecution> findById(UUID id) {
    return executionRepo.findById(id).map(this::toDomain);
  }

  @Override
  public void updateStatus(UUID executionId, String status, Long rowCount, Long durationMs) {
    executionRepo.updateStatus(executionId, status, rowCount, durationMs);
  }

  @Override
  public PageResult<ReportExecution> findByReportId(UUID reportDefinitionId, int page, int size) {
    Page<ReportExecutionEntity> result =
        executionRepo.findByReportDefinitionIdOrderByRequestedAtDesc(
            reportDefinitionId, PageRequest.of(page, size));
    List<ReportExecution> content = result.getContent().stream().map(this::toDomain).toList();
    return new PageResult<>(content, page, size, result.getTotalElements());
  }

  @Override
  public PageResult<ReportExecution> findByRequestedBy(String requestedBy, int page, int size) {
    Page<ReportExecutionEntity> result =
        executionRepo.findByRequestedByOrderByRequestedAtDesc(
            requestedBy, PageRequest.of(page, size));
    List<ReportExecution> content = result.getContent().stream().map(this::toDomain).toList();
    return new PageResult<>(content, page, size, result.getTotalElements());
  }

  private ReportExecution toDomain(ReportExecutionEntity e) {
    return new ReportExecution(
        e.getId(),
        e.getReportDefinitionId(),
        e.getReportVersionId(),
        e.getRequestedBy(),
        e.getRequestedAt(),
        e.getStatus(),
        e.getExecutionMode(),
        e.getRowCount(),
        e.getDurationMs(),
        e.getErrorCode(),
        e.getErrorMessageSanitized(),
        e.getCorrelationId());
  }
}
