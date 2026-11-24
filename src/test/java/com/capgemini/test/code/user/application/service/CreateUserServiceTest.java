package com.capgemini.test.code.user.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.capgemini.test.code.user.application.port.in.CreateUserCommand;
import com.capgemini.test.code.user.application.port.out.DniValidator;
import com.capgemini.test.code.user.application.port.out.UserNotifier;
import com.capgemini.test.code.user.application.port.out.UserRepository;
import com.capgemini.test.code.user.domain.Role;
import com.capgemini.test.code.user.domain.User;
import com.capgemini.test.code.user.domain.ValidationException;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CreateUserServiceTest {

  private static final CreateUserCommand ADMIN_COMMAND =
      new CreateUserCommand("pablo", "email@email.com", "677998899", "admin", "23454234W");
  private static final CreateUserCommand SUPERADMIN_COMMAND =
      new CreateUserCommand("pablo", "email@email.com", "677998899", "superadmin", "23454234W");

  private final UserRepository repository = mock(UserRepository.class);
  private final DniValidator dniValidator = mock(DniValidator.class);
  private final UserNotifier emailNotifier = mock(UserNotifier.class);
  private final UserNotifier smsNotifier = mock(UserNotifier.class);
  private final CreateUserService service = new CreateUserService(repository, dniValidator,
      Map.of(Role.ADMIN, emailNotifier, Role.SUPERADMIN, smsNotifier));

  @BeforeEach
  void validDniAndSaveAssignsId() {
    when(dniValidator.isValid(anyString())).thenReturn(true);
    when(repository.save(any())).thenAnswer(invocation -> {
      User user = invocation.getArgument(0);
      return new User(42L, user.name(), user.email(), user.phone(), user.role(), user.dni(), user.roomId());
    });
  }

  @Test
  void savesAdminInDefaultRoomAndNotifiesByEmail() {
    Long id = service.create(ADMIN_COMMAND);

    assertThat(id).isEqualTo(42L);
    verify(repository).save(new User(null, "pablo", "email@email.com", "677998899", Role.ADMIN, "23454234W",
        Rooms.DEFAULT_ROOM_ID));
    verify(emailNotifier).notify(any(), any());
    verifyNoInteractions(smsNotifier);
  }

  @Test
  void notifiesSuperadminBySmsWithSavedMessage() {
    service.create(SUPERADMIN_COMMAND);

    verify(smsNotifier).notify(any(), eq(CreateUserService.SAVED_MESSAGE));
    verifyNoInteractions(emailNotifier);
  }

  @Test
  void rejectsInvalidFieldsBeforeTouchingAnyDependency() {
    CreateUserCommand invalid = new CreateUserCommand("toolongname", "email@email.com", "677998899", "admin",
        "23454234W");

    assertThatThrownBy(() -> service.create(invalid)).hasMessage("error validation userName");
    verifyNoInteractions(repository, dniValidator, emailNotifier, smsNotifier);
  }

  @Test
  void rejectsDuplicateEmailWithoutCallingDniService() {
    when(repository.existsByEmail("email@email.com")).thenReturn(true);

    assertThatThrownBy(() -> service.create(ADMIN_COMMAND))
        .isInstanceOf(ValidationException.class)
        .hasMessage("error validation email");
    verifyNoInteractions(dniValidator);
    verify(repository, never()).save(any());
  }

  @Test
  void rejectsInvalidDniWithoutSavingOrNotifying() {
    when(dniValidator.isValid("23454234W")).thenReturn(false);

    assertThatThrownBy(() -> service.create(ADMIN_COMMAND))
        .isInstanceOf(ValidationException.class)
        .hasMessage("error validation dni");
    verify(repository, never()).save(any());
    verifyNoInteractions(emailNotifier, smsNotifier);
  }

  @Test
  void returnsIdEvenIfNotificationFails() {
    doThrow(new RuntimeException("notification service down")).when(emailNotifier).notify(any(), any());

    assertThat(service.create(ADMIN_COMMAND)).isEqualTo(42L);
  }
}
