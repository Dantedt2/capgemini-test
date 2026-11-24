package com.capgemini.test.code.user.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.capgemini.test.code.support.IntegrationContainers;
import com.capgemini.test.code.user.domain.Role;
import com.capgemini.test.code.user.domain.User;
import com.capgemini.test.code.user.domain.ValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaUserRepositoryAdapter.class)
class JpaUserRepositoryAdapterIT extends IntegrationContainers {

  private static final Long ROOM_1 = 1L;

  @Autowired
  private JpaUserRepositoryAdapter adapter;

  @Test
  void savesUserAndAssignsGeneratedId() {
    User saved = adapter.save(user("new@mail.com", "70000001A", "700000001"));

    assertThat(saved.id()).isNotNull();
    assertThat(adapter.findByIdAndRoomId(saved.id(), ROOM_1)).contains(saved);
  }

  @Test
  void findsExistingEmail() {
    assertThat(adapter.existsByEmail("juan@example.com")).isTrue();
    assertThat(adapter.existsByEmail("nobody@mail.com")).isFalse();
  }

  @Test
  void onlyFindsUsersInTheRequestedRoom() {
    // Seeded María (id 2) lives in room 2.
    assertThat(adapter.findByIdAndRoomId(2L, ROOM_1)).isEmpty();
    assertThat(adapter.findByIdAndRoomId(2L, 2L)).isPresent();
  }

  @Test
  void duplicateEmailIsReportedAsEmailValidationError() {
    assertDuplicate(user("juan@example.com", "70000002B", "700000002"), "email");
  }

  @Test
  void duplicateDniIsReportedAsDniValidationError() {
    assertDuplicate(user("dni@mail.com", "12345678Z", "700000003"), "dni");
  }

  @Test
  void duplicatePhoneIsReportedAsPhoneValidationError() {
    assertDuplicate(user("phone@mail.com", "70000004D", "600000001"), "phone");
  }

  private void assertDuplicate(User user, String field) {
    assertThatThrownBy(() -> adapter.save(user))
        .isInstanceOf(ValidationException.class)
        .hasMessage("error validation " + field);
  }

  private static User user(String email, String dni, String phone) {
    return new User(null, "test", email, phone, Role.ADMIN, dni, ROOM_1);
  }
}
