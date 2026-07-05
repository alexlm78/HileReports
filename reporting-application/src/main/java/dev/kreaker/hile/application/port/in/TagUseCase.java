package dev.kreaker.hile.application.port.in;

import dev.kreaker.hile.application.dto.CreateTagCommand;
import dev.kreaker.hile.application.dto.TagView;
import java.util.List;
import java.util.UUID;

public interface TagUseCase {

  TagView create(CreateTagCommand command);

  List<TagView> findAll();

  void delete(UUID id);

  List<TagView> getReportTags(UUID reportId);

  void setReportTags(UUID reportId, List<UUID> tagIds);
}
