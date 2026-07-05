package dev.kreaker.hile.infrastructure.persistence;

import dev.kreaker.hile.application.port.out.TagRepositoryPort;
import dev.kreaker.hile.domain.tag.Tag;
import dev.kreaker.hile.infrastructure.persistence.entity.ReportTagEntity;
import dev.kreaker.hile.infrastructure.persistence.entity.TagEntity;
import dev.kreaker.hile.infrastructure.persistence.jpa.ReportTagJpaRepository;
import dev.kreaker.hile.infrastructure.persistence.jpa.TagJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class TagRepositoryAdapter implements TagRepositoryPort {

  private final TagJpaRepository tagRepo;
  private final ReportTagJpaRepository reportTagRepo;

  public TagRepositoryAdapter(TagJpaRepository tagRepo, ReportTagJpaRepository reportTagRepo) {
    this.tagRepo = tagRepo;
    this.reportTagRepo = reportTagRepo;
  }

  @Override
  public Tag save(Tag tag) {
    return toDomain(tagRepo.save(new TagEntity(tag.id(), tag.name(), tag.slug())));
  }

  @Override
  public Optional<Tag> findById(UUID id) {
    return tagRepo.findById(id).map(this::toDomain);
  }

  @Override
  public List<Tag> findAll() {
    return tagRepo.findAll().stream().map(this::toDomain).toList();
  }

  @Override
  public void deleteById(UUID id) {
    tagRepo.deleteById(id);
  }

  @Override
  public boolean existsById(UUID id) {
    return tagRepo.existsById(id);
  }

  @Override
  public boolean existsByName(String name) {
    return tagRepo.existsByName(name);
  }

  @Override
  public boolean existsBySlug(String slug) {
    return tagRepo.existsBySlug(slug);
  }

  @Override
  public List<Tag> findAllByIds(List<UUID> ids) {
    return tagRepo.findByIdIn(ids).stream().map(this::toDomain).toList();
  }

  @Override
  public List<Tag> findByReportId(UUID reportId) {
    return tagRepo.findByReportDefinitionId(reportId).stream().map(this::toDomain).toList();
  }

  @Override
  @Transactional
  public void setReportTags(UUID reportId, List<UUID> tagIds) {
    reportTagRepo.deleteByIdReportDefinitionId(reportId);
    tagIds.forEach(
        tagId -> reportTagRepo.save(new ReportTagEntity(new ReportTagEntity.Id(reportId, tagId))));
  }

  private Tag toDomain(TagEntity e) {
    return new Tag(e.getId(), e.getName(), e.getSlug());
  }
}
