package dev.kreaker.hile.infrastructure.persistence.jpa;

import dev.kreaker.hile.infrastructure.persistence.entity.DataSourceEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DataSourceJpaRepository extends JpaRepository<DataSourceEntity, UUID> {
}
