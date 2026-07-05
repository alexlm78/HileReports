package dev.kreaker.hile.bootstrap.api;

import dev.kreaker.hile.application.dto.PageResult;
import java.util.List;

public record PageResponse<T>(List<T> content, int page, int size, long total, int totalPages) {

  public static <T> PageResponse<T> from(PageResult<T> result) {
    return new PageResponse<>(
        result.content(), result.page(), result.size(), result.total(), result.totalPages());
  }
}
