package dev.kreaker.hile.application.port.in;

import dev.kreaker.hile.application.dto.ChangePasswordCommand;
import dev.kreaker.hile.application.dto.CreateUserCommand;
import dev.kreaker.hile.application.dto.UserView;
import java.util.List;
import java.util.UUID;

public interface UserManagementUseCase {

  UserView create(CreateUserCommand command);

  List<UserView> findAll();

  UserView findById(UUID id);

  void disable(UUID id);

  void changePassword(ChangePasswordCommand command);
}
