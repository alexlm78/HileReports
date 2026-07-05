package dev.kreaker.hile.application.port.in;

import dev.kreaker.hile.application.dto.ChangePasswordCommand;
import dev.kreaker.hile.application.dto.CreateUserCommand;
import dev.kreaker.hile.application.dto.PageResult;
import dev.kreaker.hile.application.dto.UserView;
import java.util.List;
import java.util.UUID;

public interface UserManagementUseCase {

  UserView create(CreateUserCommand command);

  List<UserView> findAll();

  PageResult<UserView> findAllPaged(int page, int size);

  UserView findById(UUID id);

  UserView findByUsername(String username);

  void disable(UUID id);

  void changePassword(ChangePasswordCommand command);

  void changeOwnPassword(String username, String newPassword);
}
