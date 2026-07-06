package dev.kreaker.hile.application.port.out;

import dev.kreaker.hile.application.dto.PageResult;
import dev.kreaker.hile.domain.security.AppUser;
import dev.kreaker.hile.domain.security.UserCredentials;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {

  Optional<UserCredentials> findByUsername(String username);

  Optional<AppUser> findUserByUsername(String username);

  boolean existsByUsername(String username);

  void save(String username, String passwordHash, String roleName);

  AppUser createUser(String username, String passwordHash, String email, String roleName);

  List<AppUser> findAllUsers();

  PageResult<AppUser> findAllUsersPaged(int page, int size);

  Optional<AppUser> findUserById(UUID id);

  void disableUser(UUID id);

  void enableUser(UUID id);

  void changeUserPassword(UUID id, String passwordHash);

  AppUser updateUser(UUID id, String email, String roleName);
}
