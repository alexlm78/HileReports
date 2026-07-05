package dev.kreaker.hile.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "report_parameter")
public class ReportParameterEntity {

  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Column(name = "report_version_id", nullable = false, columnDefinition = "uuid")
  private UUID reportVersionId;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String label;

  @Column(name = "parameter_type", nullable = false)
  private String parameterType;

  @Column(name = "operator_type", nullable = false)
  private String operatorType;

  @Column(nullable = false)
  private boolean required;

  @Column(name = "default_value", columnDefinition = "text")
  private String defaultValue;

  @Column(name = "allows_multiple", nullable = false)
  private boolean allowsMultiple;

  @Column(name = "source_column")
  private String sourceColumn;

  @Column(name = "validation_rule", columnDefinition = "text")
  private String validationRule;

  protected ReportParameterEntity() {}

  public ReportParameterEntity(
      UUID id,
      UUID reportVersionId,
      String name,
      String label,
      String parameterType,
      String operatorType,
      boolean required,
      String defaultValue,
      boolean allowsMultiple,
      String sourceColumn,
      String validationRule) {
    this.id = id;
    this.reportVersionId = reportVersionId;
    this.name = name;
    this.label = label;
    this.parameterType = parameterType;
    this.operatorType = operatorType;
    this.required = required;
    this.defaultValue = defaultValue;
    this.allowsMultiple = allowsMultiple;
    this.sourceColumn = sourceColumn;
    this.validationRule = validationRule;
  }

  public UUID getId() {
    return id;
  }

  public UUID getReportVersionId() {
    return reportVersionId;
  }

  public String getName() {
    return name;
  }

  public String getLabel() {
    return label;
  }

  public String getParameterType() {
    return parameterType;
  }

  public String getOperatorType() {
    return operatorType;
  }

  public boolean isRequired() {
    return required;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public boolean isAllowsMultiple() {
    return allowsMultiple;
  }

  public String getSourceColumn() {
    return sourceColumn;
  }

  public String getValidationRule() {
    return validationRule;
  }
}
