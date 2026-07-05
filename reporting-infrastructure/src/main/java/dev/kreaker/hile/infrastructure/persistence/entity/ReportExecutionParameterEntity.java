package dev.kreaker.hile.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "report_execution_parameter")
public class ReportExecutionParameterEntity {

  @EmbeddedId private Id id;

  @Column(name = "parameter_value_masked", columnDefinition = "text")
  private String parameterValueMasked;

  protected ReportExecutionParameterEntity() {}

  public ReportExecutionParameterEntity(Id id, String parameterValueMasked) {
    this.id = id;
    this.parameterValueMasked = parameterValueMasked;
  }

  public Id getId() {
    return id;
  }

  public String getParameterValueMasked() {
    return parameterValueMasked;
  }

  @Embeddable
  public static class Id implements Serializable {

    @Column(name = "execution_id", columnDefinition = "uuid")
    private UUID executionId;

    @Column(name = "parameter_name")
    private String parameterName;

    protected Id() {}

    public Id(UUID executionId, String parameterName) {
      this.executionId = executionId;
      this.parameterName = parameterName;
    }

    public UUID getExecutionId() {
      return executionId;
    }

    public String getParameterName() {
      return parameterName;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof Id other)) return false;
      return Objects.equals(executionId, other.executionId)
          && Objects.equals(parameterName, other.parameterName);
    }

    @Override
    public int hashCode() {
      return Objects.hash(executionId, parameterName);
    }
  }
}
