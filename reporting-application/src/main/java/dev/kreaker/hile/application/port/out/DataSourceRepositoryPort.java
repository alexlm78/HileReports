package dev.kreaker.hile.application.port.out;

import dev.kreaker.hile.application.dto.PageResult;
import dev.kreaker.hile.application.dto.UpdateDataSourceCommand;
import dev.kreaker.hile.domain.datasource.DataSource;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DataSourceRepositoryPort {

  DataSource save(DataSource dataSource);

  Optional<DataSource> findById(UUID id);

  List<DataSource> findAll();

  PageResult<DataSource> findAllPaged(int page, int size);

  void deleteById(UUID id);

  boolean existsById(UUID id);

  DataSource update(UpdateDataSourceCommand command);
}
