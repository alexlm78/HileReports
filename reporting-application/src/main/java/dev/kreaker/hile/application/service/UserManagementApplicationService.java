package dev.kreaker.hile.application.service;

import dev.kreaker.hile.application.dto.ChangePasswordCommand;
import dev.kreaker.hile.application.dto.CreateUserCommand;
import dev.kreaker.hile.application.dto.UserView;
import dev.kreaker.hile.application.port.in.UserManagementUseCase;
import dev.kreaker.hile.application.port.out.PasswordHasherPort;
import dev.kreaker.hile.application.port.out.UserRepositoryPort;
import dev.kreaker.hile.domain.security.AppUser;
import java.util.List;
import java.util.UUID;

public class UserManagementApplicationService implements UserManagementUseCase {

  private final UserRepositoryPort repository;
  private final PasswordHasherPort passwordHasher;

  public UserManagementApplicationService(
      UserRepositoryPort repository, PasswordHasherPort passwordHasher) {
    this.repository = repository;
    this.passwordHasher = passwordHasher;
  }

  @Override
  public UserView create(CreateUserCommand command) {
    if (repository.existsByUsername(command.username())) {
      throw new IllegalStateException("Username already exists: " + command.username());
    }
    String hash = passwordHasher.hash(command.password());
    return toView(repository.createUser(command.username(), hash, command.email(), command.role()));
  }

  @Override
  public List<UserView> findAll() {
    return repository.findAllUsers().stream().map(this::toView).toList();
  }

  @Override
  public UserView findById(UUID id) {
    return repository
        .findUserById(id)
        .map(this::toView)
        .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
  }

  @Override
  public void disable(UUID id) {
    if (repository.findUserById(id).isEmpty()) {
      throw new IllegalArgumentException("User not found: " + id);
    }
    repository.disableUser(id);
  }

  @Override
  public void changePassword(ChangePasswordCommand command) {
    if (repository.findUserById(command.userId()).isEmpty()) {
      throw new IllegalArgumentException("User not found: " + command.userId());
    }
    repository.changeUserPassword(command.userId(), passwordHasher.hash(command.newPassword()));
  }

  private UserView toView(AppUser u) {
    return new UserView(u.id(), u.username(), u.email(), u.roles(), u.enabled());
  }
}
