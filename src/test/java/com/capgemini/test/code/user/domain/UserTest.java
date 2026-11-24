package com.capgemini.test.code.user.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class UserTest {

  private static final String NAME = "pablo";
  private static final String EMAIL = "email@email.com";
  private static final String PHONE = "677998899";
  private static final String ROL = "admin";
  private static final String DNI = "23454234W";
  private static final Long ROOM_ID = 1L;

  @Test
  void createsValidUser() {
    User user = User.create(NAME, EMAIL, PHONE, ROL, DNI, ROOM_ID);

    assertThat(user.id()).isNull();
    assertThat(user.name()).isEqualTo(NAME);
    assertThat(user.email()).isEqualTo(EMAIL);
    assertThat(user.phone()).isEqualTo(PHONE);
    assertThat(user.role()).isEqualTo(Role.ADMIN);
    assertThat(user.dni()).isEqualTo(DNI);
    assertThat(user.roomId()).isEqualTo(ROOM_ID);
  }

  @ParameterizedTest
  @ValueSource(strings = {"superadmin", "SUPERADMIN", "SuperAdmin"})
  void acceptsRoleInAnyCase(String rol) {
    assertThat(User.create(NAME, EMAIL, PHONE, rol, DNI, ROOM_ID).role()).isEqualTo(Role.SUPERADMIN);
  }

  @Test
  void acceptsNameWithExactlySixCharacters() {
    assertThatCode(() -> User.create("123456", EMAIL, PHONE, ROL, DNI, ROOM_ID)).doesNotThrowAnyException();
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "1234567"})
  void rejectsInvalidName(String name) {
    assertValidationError(() -> User.create(name, EMAIL, PHONE, ROL, DNI, ROOM_ID), "userName");
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"email.com", "email@email"})
  void rejectsInvalidEmail(String email) {
    assertValidationError(() -> User.create(NAME, email, PHONE, ROL, DNI, ROOM_ID), "email");
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"user", "admins"})
  void rejectsInvalidRole(String rol) {
    assertValidationError(() -> User.create(NAME, EMAIL, PHONE, rol, DNI, ROOM_ID), "rol");
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" "})
  void rejectsMissingPhone(String phone) {
    assertValidationError(() -> User.create(NAME, EMAIL, phone, ROL, DNI, ROOM_ID), "phone");
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" "})
  void rejectsMissingDni(String dni) {
    assertValidationError(() -> User.create(NAME, EMAIL, PHONE, ROL, dni, ROOM_ID), "dni");
  }

  @Test
  void reportsFirstFailingFieldWhenSeveralAreInvalid() {
    assertValidationError(() -> User.create("toolongname", "bad", PHONE, "user", DNI, ROOM_ID), "userName");
  }

  @Test
  void rebuildingExistingUserDoesNotApplyCreationRules() {
    assertThatCode(() -> new User(1L, "Juan Pérez", "juan@example.com", "600000001", Role.ADMIN, "12345678Z", 1L))
        .doesNotThrowAnyException();
  }

  private static void assertValidationError(Runnable action, String field) {
    assertThatThrownBy(action::run)
        .isInstanceOf(ValidationException.class)
        .hasMessage("error validation " + field);
  }
}
