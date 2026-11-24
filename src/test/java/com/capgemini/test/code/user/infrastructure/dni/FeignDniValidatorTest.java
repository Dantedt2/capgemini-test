package com.capgemini.test.code.user.infrastructure.dni;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import feign.FeignException;
import feign.Request;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

class FeignDniValidatorTest {

  private static final Request REQUEST =
      Request.create(Request.HttpMethod.PATCH, "/check-dni", Map.of(), null, StandardCharsets.UTF_8, null);

  private final DniClient client = mock(DniClient.class);
  private final FeignDniValidator validator = new FeignDniValidator(client);

  @Test
  void dniIsValidWhenServiceAccepts() {
    when(client.check(any())).thenReturn(ResponseEntity.ok(new CheckDniResponse("Valid DNI")));

    assertThat(validator.isValid("23454234W")).isTrue();
  }

  @Test
  void dniIsInvalidWhenServiceReturnsConflict() {
    when(client.check(any())).thenThrow(new FeignException.Conflict("Invalid DNI", REQUEST, null, null));

    assertThat(validator.isValid("99999999w")).isFalse();
  }

  @Test
  void technicalFailuresArePropagated() {
    FeignException serverError = new FeignException.InternalServerError("boom", REQUEST, null, null);
    when(client.check(any())).thenThrow(serverError);

    assertThatThrownBy(() -> validator.isValid("23454234W")).isSameAs(serverError);
  }
}
