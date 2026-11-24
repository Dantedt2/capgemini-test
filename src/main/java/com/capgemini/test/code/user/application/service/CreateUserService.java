package com.capgemini.test.code.user.application.service;

import com.capgemini.test.code.user.application.port.in.CreateUserCommand;
import com.capgemini.test.code.user.application.port.in.CreateUserUseCase;
import com.capgemini.test.code.user.application.port.out.DniValidator;
import com.capgemini.test.code.user.application.port.out.UserNotifier;
import com.capgemini.test.code.user.application.port.out.UserRepository;
import com.capgemini.test.code.user.domain.Role;
import com.capgemini.test.code.user.domain.User;
import com.capgemini.test.code.user.domain.ValidationException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateUserService implements CreateUserUseCase {

  static final String SAVED_MESSAGE = "usuario guardado";

  private static final Logger log = LoggerFactory.getLogger(CreateUserService.class);

  private final UserRepository repository;
  private final DniValidator dniValidator;
  private final Map<Role, UserNotifier> notifiers;

  public CreateUserService(UserRepository repository, DniValidator dniValidator, Map<Role, UserNotifier> notifiers) {
    this.repository = repository;
    this.dniValidator = dniValidator;
    this.notifiers = notifiers;
  }

  @Override
  public Long create(CreateUserCommand command) {
    User user = User.create(command.name(), command.email(), command.phone(), command.rol(), command.dni(),
        Rooms.DEFAULT_ROOM_ID);
    if (repository.existsByEmail(user.email())) {
      throw new ValidationException("email");
    }
    if (!dniValidator.isValid(user.dni())) {
      throw new ValidationException("dni");
    }
    User saved = repository.save(user);
    notify(saved);
    return saved.id();
  }

  private void notify(User user) {
    try {
      notifiers.get(user.role()).notify(user, SAVED_MESSAGE);
    } catch (RuntimeException e) {
      // The user is already stored; failing the request would make clients retry into a duplicate-email error.
      log.warn("Could not notify user {}", user.id(), e);
    }
  }
}
