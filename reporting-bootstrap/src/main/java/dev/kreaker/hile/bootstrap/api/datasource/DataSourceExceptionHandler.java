package dev.kreaker.hile.bootstrap.api.datasource;

import dev.kreaker.hile.application.exception.DataSourceNotFoundException;
import dev.kreaker.hile.bootstrap.api.auth.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class DataSourceExceptionHandler {

  @ExceptionHandler(DataSourceNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  ApiErrorResponse handleNotFound(DataSourceNotFoundException ex) {
    return new ApiErrorResponse("DATASOURCE_NOT_FOUND", ex.getMessage());
  }
}
