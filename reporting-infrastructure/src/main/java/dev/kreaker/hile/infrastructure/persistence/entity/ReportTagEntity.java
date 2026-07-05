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
@Table(name = "report_tag")
public class ReportTagEntity {

  @EmbeddedId private Id id;

  protected ReportTagEntity() {}

  public ReportTagEntity(Id id) {
    this.id = id;
  }

  public Id getId() {
    return id;
  }

  @Embeddable
  public static class Id implements Serializable {

    @Column(name = "report_definition_id", columnDefinition = "uuid")
    private UUID reportDefinitionId;

    @Column(name = "tag_id", columnDefinition = "uuid")
    private UUID tagId;

    protected Id() {}

    public Id(UUID reportDefinitionId, UUID tagId) {
      this.reportDefinitionId = reportDefinitionId;
      this.tagId = tagId;
    }

    public UUID getReportDefinitionId() {
      return reportDefinitionId;
    }

    public UUID getTagId() {
      return tagId;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof Id other)) return false;
      return Objects.equals(reportDefinitionId, other.reportDefinitionId)
          && Objects.equals(tagId, other.tagId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(reportDefinitionId, tagId);
    }
  }
}
