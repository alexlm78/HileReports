package dev.kreaker.hile.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "report_column")
public class ReportColumnEntity {

  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Column(name = "report_version_id", nullable = false, columnDefinition = "uuid")
  private UUID reportVersionId;

  @Column(name = "source_name", nullable = false)
  private String sourceName;

  @Column(nullable = false)
  private String label;

  @Column(name = "data_type", nullable = false)
  private String dataType;

  @Column(name = "display_type")
  private String displayType;

  @Column(name = "display_format")
  private String displayFormat;

  @Column(nullable = false)
  private int ordinal;

  @Column(name = "is_visible", nullable = false)
  private boolean visible;

  @Column(name = "is_sortable", nullable = false)
  private boolean sortable;

  @Column(name = "is_filterable_candidate", nullable = false)
  private boolean filterableCandidate;

  protected ReportColumnEntity() {}

  public ReportColumnEntity(
      UUID id,
      UUID reportVersionId,
      String sourceName,
      String label,
      String dataType,
      String displayType,
      String displayFormat,
      int ordinal,
      boolean visible,
      boolean sortable,
      boolean filterableCandidate) {
    this.id = id;
    this.reportVersionId = reportVersionId;
    this.sourceName = sourceName;
    this.label = label;
    this.dataType = dataType;
    this.displayType = displayType;
    this.displayFormat = displayFormat;
    this.ordinal = ordinal;
    this.visible = visible;
    this.sortable = sortable;
    this.filterableCandidate = filterableCandidate;
  }

  public UUID getId() {
    return id;
  }

  public UUID getReportVersionId() {
    return reportVersionId;
  }

  public String getSourceName() {
    return sourceName;
  }

  public String getLabel() {
    return label;
  }

  public String getDataType() {
    return dataType;
  }

  public String getDisplayType() {
    return displayType;
  }

  public String getDisplayFormat() {
    return displayFormat;
  }

  public int getOrdinal() {
    return ordinal;
  }

  public boolean isVisible() {
    return visible;
  }

  public boolean isSortable() {
    return sortable;
  }

  public boolean isFilterableCandidate() {
    return filterableCandidate;
  }
}
