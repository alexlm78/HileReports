package dev.kreaker.hile.infrastructure.persistence;

import dev.kreaker.hile.application.port.out.UserRepositoryPort;
import dev.kreaker.hile.domain.security.AppUser;
import dev.kreaker.hile.domain.security.UserCredentials;
import dev.kreaker.hile.infrastructure.persistence.entity.AppRoleEntity;
import dev.kreaker.hile.infrastructure.persistence.entity.AppUserEntity;
import dev.kreaker.hile.infrastructure.persistence.jpa.AppRoleJpaRepository;
import dev.kreaker.hile.infrastructure.persistence.jpa.AppUserJpaRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryAdapter implements UserRepositoryPort {

  private final AppUserJpaRepository userJpaRepository;
  private final AppRoleJpaRepository roleJpaRepository;

  public UserRepositoryAdapter(
      AppUserJpaRepository userJpaRepository, AppRoleJpaRepository roleJpaRepository) {
    this.userJpaRepository = userJpaRepository;
    this.roleJpaRepository = roleJpaRepository;
  }

  @Override
  public Optional<AppUser> findUserByUsername(String username) {
    return userJpaRepository.findByUsername(username).map(this::toDomain);
  }

  @Override
  public Optional<UserCredentials> findByUsername(String username) {
    return userJpaRepository
        .findByUsername(username)
        .filter(AppUserEntity::isEnabled)
        .map(
            entity -> {
              Set<String> roles =
                  entity.getRoles().stream()
                      .map(AppRoleEntity::getName)
                      .collect(Collectors.toSet());
              return new UserCredentials(entity.getUsername(), entity.getPasswordHash(), roles);
            });
  }

  @Override
  public boolean existsByUsername(String username) {
    return userJpaRepository.existsByUsername(username);
  }

  @Override
  public void save(String username, String passwordHash, String roleName) {
    AppRoleEntity role = requireRole(roleName);
    OffsetDateTime now = OffsetDateTime.now();
    AppUserEntity user =
        new AppUserEntity(UUID.randomUUID(), username, passwordHash, null, now, now);
    user.getRoles().add(role);
    userJpaRepository.save(user);
  }

  @Override
  public AppUser createUser(String username, String passwordHash, String email, String roleName) {
    AppRoleEntity role = requireRole(roleName);
    OffsetDateTime now = OffsetDateTime.now();
    AppUserEntity user =
        new AppUserEntity(UUID.randomUUID(), username, passwordHash, email, now, now);
    user.getRoles().add(role);
    return toDomain(userJpaRepository.save(user));
  }

  @Override
  public List<AppUser> findAllUsers() {
    return userJpaRepository.findAll().stream().map(this::toDomain).toList();
  }

  @Override
  public Optional<AppUser> findUserById(UUID id) {
    return userJpaRepository.findById(id).map(this::toDomain);
  }

  @Override
  public void disableUser(UUID id) {
    userJpaRepository
        .findById(id)
        .ifPresent(
            u -> {
              u.disable();
              userJpaRepository.save(u);
            });
  }

  @Override
  public void changeUserPassword(UUID id, String passwordHash) {
    userJpaRepository
        .findById(id)
        .ifPresent(
            u -> {
              u.changePasswordHash(passwordHash);
              userJpaRepository.save(u);
            });
  }

  private AppRoleEntity requireRole(String roleName) {
    return roleJpaRepository
        .findByName(roleName)
        .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));
  }

  private AppUser toDomain(AppUserEntity e) {
    Set<String> roles =
        e.getRoles().stream().map(AppRoleEntity::getName).collect(Collectors.toSet());
    return new AppUser(e.getId(), e.getUsername(), e.getEmail(), roles, e.isEnabled());
  }
}
