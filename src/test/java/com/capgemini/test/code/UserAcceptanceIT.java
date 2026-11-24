package com.capgemini.test.code;

import static org.assertj.core.api.Assertions.assertThat;

import com.capgemini.test.code.support.IntegrationContainers;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** README scenarios, end to end over HTTP against real Postgres and the provided mock-server contract. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserAcceptanceIT extends IntegrationContainers {

  private static final AtomicInteger SEQUENCE = new AtomicInteger();

  @Autowired
  private TestRestTemplate http;

  @Test
  void readmeExampleIsCreatedWith201AndItsId() {
    NewUser readmeExample = new NewUser("pablo", "email@email.com", "677998899", "admin", "23454234W");

    ResponseEntity<Map> response = create(readmeExample);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    long id = idOf(response);
    assertThat(response.getHeaders().getLocation()).hasToString("/users/" + id);
  }

  @Test
  void createdAdminCanBeRetrievedAndIsNotifiedByEmail() {
    NewUser admin = uniqueUser("admin");

    long id = idOf(create(admin));

    ResponseEntity<Map> found = http.getForEntity("/users/" + id, Map.class);
    assertThat(found.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(found.getBody()).isEqualTo(Map.of("id", (int) id, "name", admin.name(), "email", admin.email(),
        "phone", admin.phone(), "rol", "admin", "dni", admin.dni()));
    assertMockServerReceivedOnce("/email", """
        {"email": "%s", "message": "usuario guardado"}""".formatted(admin.email()));
  }

  @Test
  void createdSuperadminIsNotifiedBySms() {
    NewUser superadmin = uniqueUser("superadmin");

    assertThat(create(superadmin).getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertMockServerReceivedOnce("/sms", """
        {"phone": "%s", "message": "usuario guardado"}""".formatted(superadmin.phone()));
  }

  @ParameterizedTest(name = "{0}={1} -> error validation {2}")
  @CsvSource({
      "name,  pablito,       userName",
      "email, no-at.mail.com, email",
      "email, nodot@mailcom,  email",
      "rol,   user,          rol",
      "dni,   99999999w,     dni"})
  void invalidFieldIsRejectedWith409(String field, String value, String errorField) {
    NewUser user = uniqueUser("admin").with(field, value);

    assertError(create(user), 409, "error validation " + errorField);
  }

  @Test
  void duplicateEmailIsRejectedWith409() {
    NewUser first = uniqueUser("admin");
    create(first);

    assertError(create(uniqueUser("admin").with("email", first.email())), 409, "error validation email");
  }

  @Test
  void duplicateDniIsRejectedWith409() {
    NewUser first = uniqueUser("admin");
    create(first);

    assertError(create(uniqueUser("admin").with("dni", first.dni())), 409, "error validation dni");
  }

  @Test
  void duplicatePhoneIsRejectedWith409() {
    NewUser first = uniqueUser("admin");
    create(first);

    assertError(create(uniqueUser("admin").with("phone", first.phone())), 409, "error validation phone");
  }

  @Test
  void seededUserInRoom1IsFound() {
    ResponseEntity<Map> response = http.getForEntity("/users/1", Map.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).containsEntry("email", "juan@example.com");
  }

  @Test
  void userFromAnotherRoomIsNotFound() {
    // Seeded María (id 2) lives in room 2.
    assertError(http.getForEntity("/users/2", Map.class), 404, "user not found");
  }

  @Test
  void unknownUserIsNotFound() {
    assertError(http.getForEntity("/users/999999", Map.class), 404, "user not found");
  }

  private ResponseEntity<Map> create(NewUser user) {
    return http.postForEntity("/users", user, Map.class);
  }

  private static long idOf(ResponseEntity<Map> response) {
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    return ((Number) response.getBody().get("id")).longValue();
  }

  private static void assertError(ResponseEntity<Map> response, int code, String message) {
    assertThat(response.getStatusCode().value()).isEqualTo(code);
    assertThat(response.getBody()).isEqualTo(Map.of("code", code, "message", message));
  }

  private static NewUser uniqueUser(String rol) {
    int n = SEQUENCE.incrementAndGet();
    return new NewUser("user", "user" + n + "@mail.com", "7%08d".formatted(n), rol, "%08dT".formatted(n));
  }

  private record NewUser(String name, String email, String phone, String rol, String dni) {

    NewUser with(String field, String value) {
      return switch (field) {
        case "name" -> new NewUser(value, email, phone, rol, dni);
        case "email" -> new NewUser(name, value, phone, rol, dni);
        case "phone" -> new NewUser(name, email, value, rol, dni);
        case "rol" -> new NewUser(name, email, phone, value, dni);
        case "dni" -> new NewUser(name, email, phone, rol, value);
        default -> throw new IllegalArgumentException(field);
      };
    }
  }
}
