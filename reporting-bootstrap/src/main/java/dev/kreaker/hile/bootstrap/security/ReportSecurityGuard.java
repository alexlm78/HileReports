package dev.kreaker.hile.bootstrap.security;

import dev.kreaker.hile.application.port.in.CreateReportDefinitionUseCase;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("reportSecurity")
public class ReportSecurityGuard {

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
}
