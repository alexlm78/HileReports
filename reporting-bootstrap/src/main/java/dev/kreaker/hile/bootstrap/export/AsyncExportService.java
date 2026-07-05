package dev.kreaker.hile.bootstrap.export;

import dev.kreaker.hile.application.dto.AsyncExportTask;
import dev.kreaker.hile.application.dto.PreviewResult;
import dev.kreaker.hile.application.port.in.DataSourceUseCase;
import dev.kreaker.hile.application.port.out.MetricsPort;
import dev.kreaker.hile.application.port.out.ReportExecutionRepository;
import dev.kreaker.hile.application.port.out.ReportExportRepository;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncExportService {

  private final DataSourceUseCase dataSourceUseCase;
  private final ReportExportRepository exportRepository;
  private final ReportExecutionRepository executionRepository;
  private final MetricsPort metrics;

  public AsyncExportService(
      DataSourceUseCase dataSourceUseCase,
      ReportExportRepository exportRepository,
      ReportExecutionRepository executionRepository,
      MetricsPort metrics) {
    this.dataSourceUseCase = dataSourceUseCase;
    this.exportRepository = exportRepository;
    this.executionRepository = executionRepository;
    this.metrics = metrics;
  }

  @Async("exportTaskExecutor")
  public void processAsync(AsyncExportTask task) {
    long startMs = System.currentTimeMillis();
    try {
      PreviewResult result =
          dataSourceUseCase.executeWithParams(
              task.dataSourceId(), task.boundSql(), task.paramValues(), task.pageSize(), 0);

      byte[] fileBytes =
          switch (task.format()) {
            case "CSV" -> generateCsv(result);
            case "XLSX" -> generateXlsx(result);
            default -> throw new IllegalArgumentException("Unsupported format: " + task.format());
          };

      Path filePath = Path.of(task.storagePath());
      Files.createDirectories(filePath.getParent());
      Files.write(filePath, fileBytes);

      long duration = System.currentTimeMillis() - startMs;
      exportRepository.updateStatusAndPath(task.exportId(), "COMPLETED", task.storagePath());
      executionRepository.updateStatus(
          task.executionId(), "COMPLETED", (long) result.rows().size(), duration);
      metrics.recordExport(task.format(), "COMPLETED", duration);

    } catch (Exception e) {
      long duration = System.currentTimeMillis() - startMs;
      exportRepository.updateStatus(task.exportId(), "FAILED");
      executionRepository.updateStatus(task.executionId(), "FAILED", null, duration);
      metrics.recordExport(task.format(), "FAILED", duration);
    }
  }

  private byte[] generateCsv(PreviewResult result) throws IOException {
    String[] headers =
        result.columns().stream()
            .map(dev.kreaker.hile.application.dto.ColumnMetadata::label)
            .toArray(String[]::new);
    StringWriter sw = new StringWriter();
    try (CSVPrinter printer =
        new CSVPrinter(sw, CSVFormat.DEFAULT.builder().setHeader(headers).build())) {
      for (List<Object> row : result.rows()) {
        printer.printRecord(row);
      }
    }
    return sw.toString().getBytes(StandardCharsets.UTF_8);
  }

  private byte[] generateXlsx(PreviewResult result) throws IOException {
    try (Workbook workbook = new XSSFWorkbook()) {
      Sheet sheet = workbook.createSheet("Report");

      Row header = sheet.createRow(0);
      for (int i = 0; i < result.columns().size(); i++) {
        header.createCell(i).setCellValue(result.columns().get(i).label());
      }

      List<List<Object>> rows = result.rows();
      for (int r = 0; r < rows.size(); r++) {
        Row row = sheet.createRow(r + 1);
        List<Object> rowData = rows.get(r);
        for (int c = 0; c < rowData.size(); c++) {
          Object val = rowData.get(c);
          if (val != null) {
            row.createCell(c).setCellValue(val.toString());
          }
        }
      }

      try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
        workbook.write(bos);
        return bos.toByteArray();
      }
    }
  }
}
