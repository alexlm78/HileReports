package dev.kreaker.hile.bootstrap.api.user;

import dev.kreaker.hile.application.dto.ChangePasswordCommand;
import dev.kreaker.hile.application.dto.CreateUserCommand;
import dev.kreaker.hile.application.dto.UserView;
import dev.kreaker.hile.application.port.in.UserManagementUseCase;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

  private final UserManagementUseCase userManagement;

  public UserController(UserManagementUseCase userManagement) {
    this.userManagement = userManagement;
  }

  @PostMapping
  public ResponseEntity<UserView> create(@Valid @RequestBody CreateUserRequest request) {
    UserView view =
        userManagement.create(
            new CreateUserCommand(
                request.username(), request.password(), request.email(), request.role()));
    return ResponseEntity.created(URI.create("/api/v1/users/" + view.id())).body(view);
  }

  @GetMapping
  public ResponseEntity<List<UserView>> findAll() {
    return ResponseEntity.ok(userManagement.findAll());
  }

  @GetMapping("/{id}")
  public ResponseEntity<UserView> findById(@PathVariable UUID id) {
    return ResponseEntity.ok(userManagement.findById(id));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> disable(@PathVariable UUID id) {
    userManagement.disable(id);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{id}/password")
  public ResponseEntity<Void> changePassword(
      @PathVariable UUID id, @Valid @RequestBody ChangePasswordRequest request) {
    userManagement.changePassword(new ChangePasswordCommand(id, request.newPassword()));
    return ResponseEntity.noContent().build();
  }
}
