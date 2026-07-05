package dev.kreaker.hile.application.service;

import dev.kreaker.hile.application.dto.CreateTagCommand;
import dev.kreaker.hile.application.dto.TagView;
import dev.kreaker.hile.application.port.in.TagUseCase;
import dev.kreaker.hile.application.port.out.TagRepositoryPort;
import dev.kreaker.hile.domain.tag.Tag;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class TagApplicationService implements TagUseCase {

  private final TagRepositoryPort repository;

  public TagApplicationService(TagRepositoryPort repository) {
    this.repository = repository;
  }

  @Override
  public TagView create(CreateTagCommand command) {
    String name = command.name().strip();
    if (repository.existsByName(name)) {
      throw new IllegalStateException("Tag already exists: " + name);
    }
    String slug = toSlug(name);
    if (repository.existsBySlug(slug)) {
      throw new IllegalStateException("Tag slug conflict: " + slug);
    }
    return toView(repository.save(new Tag(UUID.randomUUID(), name, slug)));
  }

  @Override
  public List<TagView> findAll() {
    return repository.findAll().stream().map(this::toView).toList();
  }

  @Override
  public void delete(UUID id) {
    if (!repository.existsById(id)) {
      throw new IllegalArgumentException("Tag not found: " + id);
    }
    repository.deleteById(id);
  }

  @Override
  public List<TagView> getReportTags(UUID reportId) {
    return repository.findByReportId(reportId).stream().map(this::toView).toList();
  }

  @Override
  public void setReportTags(UUID reportId, List<UUID> tagIds) {
    if (tagIds == null || tagIds.isEmpty()) {
      repository.setReportTags(reportId, List.of());
      return;
    }
    Set<UUID> unique = Set.copyOf(tagIds);
    List<Tag> found = repository.findAllByIds(List.copyOf(unique));
    if (found.size() != unique.size()) {
      Set<UUID> foundIds = found.stream().map(Tag::id).collect(Collectors.toSet());
      Set<UUID> missing =
          unique.stream().filter(id -> !foundIds.contains(id)).collect(Collectors.toSet());
      throw new IllegalArgumentException("Tags not found: " + missing);
    }
    repository.setReportTags(reportId, List.copyOf(unique));
  }

  private TagView toView(Tag t) {
    return new TagView(t.id(), t.name(), t.slug());
  }

  private static String toSlug(String name) {
    return name.toLowerCase().replaceAll("[^a-z0-9\\s-]", "").trim().replaceAll("[\\s-]+", "-");
  }
}
