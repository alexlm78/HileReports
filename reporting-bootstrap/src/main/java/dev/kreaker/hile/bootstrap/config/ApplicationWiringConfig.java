package dev.kreaker.hile.bootstrap.config;

import dev.kreaker.hile.application.port.in.AuthenticateUserUseCase;
import dev.kreaker.hile.application.port.in.CategoryUseCase;
import dev.kreaker.hile.application.port.in.CreateReportDefinitionUseCase;
import dev.kreaker.hile.application.port.in.DataSourceUseCase;
import dev.kreaker.hile.application.port.in.ExecuteReportUseCase;
import dev.kreaker.hile.application.port.in.ExportJobUseCase;
import dev.kreaker.hile.application.port.in.TagUseCase;
import dev.kreaker.hile.application.port.out.AuthenticationProviderPort;
import dev.kreaker.hile.application.port.out.CategoryRepositoryPort;
import dev.kreaker.hile.application.port.out.DataSourceRepositoryPort;
import dev.kreaker.hile.application.port.out.DbConnectorPort;
import dev.kreaker.hile.application.port.out.MetricsPort;
import dev.kreaker.hile.application.port.out.PasswordEncryptionPort;
import dev.kreaker.hile.application.port.out.QueryValidatorPort;
import dev.kreaker.hile.application.port.out.ReportColumnRepositoryPort;
import dev.kreaker.hile.application.port.out.ReportDefinitionRepository;
import dev.kreaker.hile.application.port.out.ReportExecutionRepository;
import dev.kreaker.hile.application.port.out.ReportExportRepository;
import dev.kreaker.hile.application.port.out.ReportParameterRepositoryPort;
import dev.kreaker.hile.application.port.out.TagRepositoryPort;
import dev.kreaker.hile.application.port.out.UserRepositoryPort;
import dev.kreaker.hile.application.service.AuthenticationApplicationService;
import dev.kreaker.hile.application.service.CategoryApplicationService;
import dev.kreaker.hile.application.service.DataSourceApplicationService;
import dev.kreaker.hile.application.service.ExecuteReportApplicationService;
import dev.kreaker.hile.application.service.ExportJobApplicationService;
import dev.kreaker.hile.application.service.ReportDefinitionApplicationService;
import dev.kreaker.hile.application.service.TagApplicationService;
import dev.kreaker.hile.security.AdAuthenticationProviderStub;
import dev.kreaker.hile.security.LocalAuthenticationProviderAdapter;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class ApplicationWiringConfig {

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  AuthenticationProviderPort authenticationProviderPort(
      @Value("${hile.reports.security.mode:local}") String securityMode,
      UserRepositoryPort userRepositoryPort) {
    if ("ad".equalsIgnoreCase(securityMode)) {
      return new AdAuthenticationProviderStub();
    }
    return new LocalAuthenticationProviderAdapter(userRepositoryPort);
  }

  @Bean
  AuthenticateUserUseCase authenticateUserUseCase(
      AuthenticationProviderPort authenticationProviderPort) {
    return new AuthenticationApplicationService(authenticationProviderPort);
  }

  @Bean
  DataSourceUseCase dataSourceUseCase(
      DataSourceRepositoryPort dataSourceRepositoryPort,
      PasswordEncryptionPort passwordEncryptionPort,
      List<DbConnectorPort> connectors) {
    return new DataSourceApplicationService(
        dataSourceRepositoryPort, passwordEncryptionPort, connectors);
  }

  @Bean
  CreateReportDefinitionUseCase createReportDefinitionUseCase(
      ReportDefinitionRepository reportDefinitionRepository,
      QueryValidatorPort queryValidatorPort,
      DataSourceUseCase dataSourceUseCase,
      ReportColumnRepositoryPort reportColumnRepositoryPort,
      ReportParameterRepositoryPort reportParameterRepositoryPort,
      @Value("${hile.reports.preview.max-rows:100}") int maxRows,
      @Value("${hile.reports.execution.default-timeout-seconds:30}") int timeoutSeconds) {
    return new ReportDefinitionApplicationService(
        reportDefinitionRepository,
        queryValidatorPort,
        dataSourceUseCase,
        reportColumnRepositoryPort,
        reportParameterRepositoryPort,
        maxRows,
        timeoutSeconds);
  }

  @Bean
  CategoryUseCase categoryUseCase(CategoryRepositoryPort categoryRepositoryPort) {
    return new CategoryApplicationService(categoryRepositoryPort);
  }

  @Bean
  ExportJobUseCase exportJobUseCase(
      ReportDefinitionRepository reportDefinitionRepository,
      ReportParameterRepositoryPort reportParameterRepositoryPort,
      ReportExecutionRepository reportExecutionRepository,
      ReportExportRepository reportExportRepository,
      @Value("${hile.reports.export.storage-path:/tmp/hile-exports}") String storageBasePath,
      @Value("${hile.reports.export.expiry-hours:24}") int expiryHours) {
    return new ExportJobApplicationService(
        reportDefinitionRepository,
        reportParameterRepositoryPort,
        reportExecutionRepository,
        reportExportRepository,
        storageBasePath,
        expiryHours);
  }

  @Bean
  TagUseCase tagUseCase(TagRepositoryPort tagRepositoryPort) {
    return new TagApplicationService(tagRepositoryPort);
  }

  @Bean
  ExecuteReportUseCase executeReportUseCase(
      ReportDefinitionRepository reportDefinitionRepository,
      ReportParameterRepositoryPort reportParameterRepositoryPort,
      DataSourceUseCase dataSourceUseCase,
      ReportExecutionRepository reportExecutionRepository,
      MetricsPort metricsPort) {
    return new ExecuteReportApplicationService(
        reportDefinitionRepository,
        reportParameterRepositoryPort,
        dataSourceUseCase,
        reportExecutionRepository,
        metricsPort);
  }
}
