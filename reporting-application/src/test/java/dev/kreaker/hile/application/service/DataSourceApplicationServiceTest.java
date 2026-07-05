package dev.kreaker.hile.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import dev.kreaker.hile.application.dto.CreateDataSourceCommand;
import dev.kreaker.hile.application.dto.DataSourceView;
import dev.kreaker.hile.application.dto.ValidationResult;
import dev.kreaker.hile.application.exception.DataSourceNotFoundException;
import dev.kreaker.hile.application.port.out.DataSourceRepositoryPort;
import dev.kreaker.hile.application.port.out.DbConnectorPort;
import dev.kreaker.hile.application.port.out.PasswordEncryptionPort;
import dev.kreaker.hile.domain.datasource.DataSource;
import dev.kreaker.hile.domain.datasource.DataSourceStatus;
import dev.kreaker.hile.domain.datasource.DataSourceType;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DataSourceApplicationServiceTest {

  @Mock private DataSourceRepositoryPort repository;
  @Mock private PasswordEncryptionPort encryption;
  @Mock private DbConnectorPort pgConnector;

  private DataSourceApplicationService service;

  @BeforeEach
  void setUp() {
    given(pgConnector.supports(DataSourceType.POSTGRESQL)).willReturn(true);
    given(pgConnector.supports(DataSourceType.MYSQL)).willReturn(false);
    given(pgConnector.supports(DataSourceType.ORACLE)).willReturn(false);
    service = new DataSourceApplicationService(repository, encryption, List.of(pgConnector));
  }

  @Test
  void shouldCreateDataSourceWithEncryptedPassword() {
    given(encryption.encrypt("secret123")).willReturn("enc:v1:abc:def");
    given(repository.save(any())).willAnswer(inv -> inv.getArgument(0));

    CreateDataSourceCommand command =
        new CreateDataSourceCommand(
            "prod-db",
            DataSourceType.POSTGRESQL,
            "localhost",
            5432,
            "mydb",
            "admin",
            "secret123",
            null,
            "user1");

    DataSourceView view = service.create(command);

    assertThat(view.name()).isEqualTo("prod-db");
    assertThat(view.dbType()).isEqualTo(DataSourceType.POSTGRESQL);
    assertThat(view.username()).isEqualTo("admin");
    assertThat(view.status()).isEqualTo(DataSourceStatus.ACTIVE);
    verify(encryption).encrypt("secret123");
  }

  @Test
  void shouldThrowWhenDataSourceNotFound() {
    UUID id = UUID.randomUUID();
    given(repository.findById(id)).willReturn(Optional.empty());

    assertThatThrownBy(() -> service.findById(id)).isInstanceOf(DataSourceNotFoundException.class);
  }

  @Test
  void shouldTestConnectionSuccessfully() {
    UUID id = UUID.randomUUID();
    DataSource ds =
        new DataSource(
            id,
            "prod-db",
            DataSourceType.POSTGRESQL,
            "localhost",
            5432,
            "mydb",
            "admin",
            "enc:v1:abc:def",
            null,
            DataSourceStatus.ACTIVE,
            "user1",
            OffsetDateTime.now());

    given(repository.findById(id)).willReturn(Optional.of(ds));
    given(encryption.decrypt("enc:v1:abc:def")).willReturn("secret123");
    given(pgConnector.testConnection("jdbc:postgresql://localhost:5432/mydb", "admin", "secret123"))
        .willReturn(ValidationResult.ok());

    ValidationResult result = service.testConnection(id);

    assertThat(result.valid()).isTrue();
  }
}
