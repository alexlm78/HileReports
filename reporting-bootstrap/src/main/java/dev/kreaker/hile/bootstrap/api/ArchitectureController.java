package dev.kreaker.hile.bootstrap.api;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/architecture")
public class ArchitectureController {

  @GetMapping("/modules")
  public Map<String, Object> modules() {
    return Map.of(
        "application",
        "Hile Reports",
        "modules",
        List.of(
            "reporting-domain",
            "reporting-application",
            "reporting-infrastructure",
            "reporting-connectors",
            "reporting-security",
            "reporting-jobs",
            "reporting-bootstrap"));
  }
}
