package dev.kreaker.hile.application.port.in;

import dev.kreaker.hile.application.dto.ExecuteReportCommand;
import dev.kreaker.hile.application.dto.ExecutionResultView;
import dev.kreaker.hile.application.dto.ExecutionView;
import dev.kreaker.hile.application.dto.PageResult;
import java.util.UUID;

public interface ExecuteReportUseCase {

  ExecutionResultView execute(ExecuteReportCommand command, String requestedBy);

  PageResult<ExecutionView> listByReport(UUID reportId, String requestedBy, int page, int size);

  PageResult<ExecutionView> listMine(String requestedBy, int page, int size);
}
