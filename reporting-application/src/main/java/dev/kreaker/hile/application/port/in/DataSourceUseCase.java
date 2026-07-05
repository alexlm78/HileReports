package dev.kreaker.hile.application.port.in;

import dev.kreaker.hile.application.dto.ColumnMetadata;
import dev.kreaker.hile.application.dto.CreateDataSourceCommand;
import dev.kreaker.hile.application.dto.DataSourceView;
import dev.kreaker.hile.application.dto.PreviewResult;
import dev.kreaker.hile.application.dto.ValidationResult;
import java.util.List;
import java.util.UUID;

public interface DataSourceUseCase {

  DataSourceView create(CreateDataSourceCommand command);

  DataSourceView findById(UUID id);

  List<DataSourceView> findAll();

  void delete(UUID id);

  ValidationResult testConnection(UUID id);

  List<ColumnMetadata> discoverColumns(UUID dataSourceId, String sqlText);

  PreviewResult executePreview(UUID dataSourceId, String sqlText, int maxRows);

  PreviewResult executeWithParams(
      UUID dataSourceId, String sqlText, List<Object> paramValues, int pageSize, int offset);
}
