package dev.kreaker.hile.bootstrap.security;

import dev.kreaker.hile.application.port.in.CreateReportDefinitionUseCase;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("reportSecurity")
public class ReportSecurityGuard {

  private static final Set<String> EXECUTE_ROLES =
      Set.of("ROLE_PLATFORM_ADMIN", "ROLE_REPORT_DESIGNER", "ROLE_REPORT_VIEWER");

  private final CreateReportDefinitionUseCase reportUseCase;

  public ReportSecurityGuard(CreateReportDefinitionUseCase reportUseCase) {
    this.reportUseCase = reportUseCase;
  }

  public boolean isOwnerOrAdmin(UUID reportId, Authentication auth) {
    if (auth == null) return false;
    boolean isAdmin =
        auth.getAuthorities().stream()
            .anyMatch(a -> "ROLE_PLATFORM_ADMIN".equals(a.getAuthority()));
    if (isAdmin) return true;
    return reportUseCase
        .findById(reportId)
        .map(r -> auth.getName().equals(r.createdBy()))
        .orElse(false);
  }

  public boolean canExecute(Authentication auth) {
    if (auth == null) return false;
    return auth.getAuthorities().stream().anyMatch(a -> EXECUTE_ROLES.contains(a.getAuthority()));
  }
}
