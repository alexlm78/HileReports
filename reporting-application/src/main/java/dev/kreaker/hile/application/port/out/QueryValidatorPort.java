package dev.kreaker.hile.application.port.out;

import dev.kreaker.hile.application.dto.ValidationResult;
import java.util.List;

public interface QueryValidatorPort {

  ValidationResult validateReadOnly(String sqlText);

  List<String> extractNamedParameters(String sqlText);
}
