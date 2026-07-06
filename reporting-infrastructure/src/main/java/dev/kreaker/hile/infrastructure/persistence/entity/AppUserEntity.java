package dev.kreaker.hile.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "app_user")
public class AppUserEntity {

  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Column(nullable = false, unique = true)
  private String username;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Column private String email;

  @Column(nullable = false)
  private boolean enabled = true;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "user_role",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id"))
  private Set<AppRoleEntity> roles = new HashSet<>();

  protected AppUserEntity() {}

  public AppUserEntity(
      UUID id,
      String username,
      String passwordHash,
      String email,
      OffsetDateTime createdAt,
      OffsetDateTime updatedAt) {
    this.id = id;
    this.username = username;
    this.passwordHash = passwordHash;
    this.email = email;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public UUID getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public String getEmail() {
    return email;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }

  public Set<AppRoleEntity> getRoles() {
    return roles;
  }

  public void disable() {
    this.enabled = false;
    this.updatedAt = OffsetDateTime.now();
  }

  public void enable() {
    this.enabled = true;
    this.updatedAt = OffsetDateTime.now();
  }

  public void changePasswordHash(String newHash) {
    this.passwordHash = newHash;
    this.updatedAt = OffsetDateTime.now();
  }

  public void setEmail(String email) {
    this.email = email;
    this.updatedAt = OffsetDateTime.now();
  }
}
