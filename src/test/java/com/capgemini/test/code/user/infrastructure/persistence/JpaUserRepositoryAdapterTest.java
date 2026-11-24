package com.capgemini.test.code.user.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.capgemini.test.code.user.domain.Role;
import com.capgemini.test.code.user.domain.User;
import com.capgemini.test.code.user.domain.ValidationException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

class JpaUserRepositoryAdapterTest {

  private static final User USER =
      new User(null, "pablo", "email@email.com", "677998899", Role.ADMIN, "23454234W", 1L);

  private final SpringDataUserRepository jpa = mock(SpringDataUserRepository.class);
  private final JpaUserRepositoryAdapter adapter = new JpaUserRepositoryAdapter(jpa);

  @ParameterizedTest
  @CsvSource({"users_email_key, email", "users_dni_key, dni", "users_phone_key, phone"})
  void translatesUniqueViolationIntoValidationErrorForThatField(String constraint, String field) {
    when(jpa.saveAndFlush(any())).thenThrow(uniqueViolation(constraint));

    assertThatThrownBy(() -> adapter.save(USER))
        .isInstanceOf(ValidationException.class)
        .hasMessage("error validation " + field);
  }

  @Test
  void rethrowsViolationsOfUnknownConstraints() {
    DataIntegrityViolationException violation = uniqueViolation("some_other_constraint");
    when(jpa.saveAndFlush(any())).thenThrow(violation);

    assertThatThrownBy(() -> adapter.save(USER)).isSameAs(violation);
  }

  private static DataIntegrityViolationException uniqueViolation(String constraint) {
    return new DataIntegrityViolationException("could not execute statement",
        new RuntimeException("ERROR: duplicate key value violates unique constraint \"" + constraint + "\""));
  }
}
