package dev.kreaker.hile.application.port.out;

import dev.kreaker.hile.domain.tag.Tag;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TagRepositoryPort {

  Tag save(Tag tag);

  Optional<Tag> findById(UUID id);

  List<Tag> findAll();

  void deleteById(UUID id);

  boolean existsById(UUID id);

  boolean existsByName(String name);

  boolean existsBySlug(String slug);

  List<Tag> findAllByIds(List<UUID> ids);

  List<Tag> findByReportId(UUID reportId);

  void setReportTags(UUID reportId, List<UUID> tagIds);
}
