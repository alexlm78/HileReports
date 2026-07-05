package dev.kreaker.hile.application.port.in;

import dev.kreaker.hile.application.dto.ExportJobCreationResult;
import dev.kreaker.hile.application.dto.ExportJobView;
import dev.kreaker.hile.application.dto.RequestExportCommand;
import java.util.UUID;

public interface ExportJobUseCase {

  ExportJobCreationResult requestExport(RequestExportCommand command, String requestedBy);

  ExportJobView getExport(UUID exportId);

  String getExportFilePath(UUID exportId);
}
