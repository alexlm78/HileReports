package dev.kreaker.hile.application.port.in;

import dev.kreaker.hile.application.dto.ExecuteReportCommand;
import dev.kreaker.hile.application.dto.ExecutionResultView;

public interface ExecuteReportUseCase {

  ExecutionResultView execute(ExecuteReportCommand command, String requestedBy);
}
