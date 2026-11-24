package com.capgemini.test.code.user.infrastructure.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.capgemini.test.code.user.application.port.in.CreateUserCommand;
import com.capgemini.test.code.user.application.port.in.CreateUserUseCase;
import com.capgemini.test.code.user.application.port.in.GetUserUseCase;
import com.capgemini.test.code.user.domain.Role;
import com.capgemini.test.code.user.domain.User;
import com.capgemini.test.code.user.domain.UserNotFoundException;
import com.capgemini.test.code.user.domain.ValidationException;
import feign.FeignException;
import feign.Request;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(UserController.class)
class UserControllerTest {

  private static final String README_EXAMPLE = """
      {"name": "pablo", "email": "email@email.com", "phone": "677998899", "rol": "admin", "dni": "23454234W"}""";

  @Autowired
  private MockMvc mvc;
  @MockBean
  private CreateUserUseCase createUser;
  @MockBean
  private GetUserUseCase getUser;

  @Test
  void createReturns201WithIdAndLocation() throws Exception {
    when(createUser.create(new CreateUserCommand("pablo", "email@email.com", "677998899", "admin", "23454234W")))
        .thenReturn(7L);

    postUser(README_EXAMPLE)
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/users/7"))
        .andExpect(content().json("{\"id\": 7}", true));
  }

  @Test
  void getReturnsUserWithLowercaseRol() throws Exception {
    when(getUser.get(1L)).thenReturn(
        new User(1L, "pablo", "email@email.com", "677998899", Role.SUPERADMIN, "23454234W", 1L));

    mvc.perform(get("/users/1"))
        .andExpect(status().isOk())
        .andExpect(content().json("""
            {"id": 1, "name": "pablo", "email": "email@email.com", "phone": "677998899",
             "rol": "superadmin", "dni": "23454234W"}""", true));
  }

  @Test
  void validationErrorReturns409() throws Exception {
    when(createUser.create(any())).thenThrow(new ValidationException("email"));

    expectError(postUser(README_EXAMPLE), 409, "error validation email");
  }

  @Test
  void unknownUserReturns404() throws Exception {
    when(getUser.get(99L)).thenThrow(new UserNotFoundException());

    expectError(mvc.perform(get("/users/99")), 404, "user not found");
  }

  @Test
  void malformedJsonReturns400() throws Exception {
    expectError(postUser("{bad"), 400, "malformed request");
  }

  @Test
  void nonNumericIdReturns400() throws Exception {
    expectError(mvc.perform(get("/users/abc")), 400, "malformed request");
  }

  @Test
  void dniServiceFailureReturns502() throws Exception {
    Request request = Request.create(Request.HttpMethod.PATCH, "/check-dni", Map.of(), null,
        StandardCharsets.UTF_8, null);
    when(createUser.create(any())).thenThrow(new FeignException.ServiceUnavailable("down", request, null, null));

    expectError(postUser(README_EXAMPLE), 502, "dni validation unavailable");
  }

  @Test
  void unexpectedErrorReturns500WithoutDetails() throws Exception {
    when(createUser.create(any())).thenThrow(new IllegalStateException("secret internals"));

    expectError(postUser(README_EXAMPLE), 500, "internal error");
  }

  private ResultActions postUser(String body) throws Exception {
    return mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(body));
  }

  private static void expectError(ResultActions result, int code, String message) throws Exception {
    result.andExpect(status().is(code))
        .andExpect(content().json("{\"code\": %d, \"message\": \"%s\"}".formatted(code, message), true));
  }
}
