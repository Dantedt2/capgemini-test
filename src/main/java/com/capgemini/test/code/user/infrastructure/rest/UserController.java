package com.capgemini.test.code.user.infrastructure.rest;

import com.capgemini.test.code.user.application.port.in.CreateUserUseCase;
import com.capgemini.test.code.user.application.port.in.GetUserUseCase;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
class UserController {

  private final CreateUserUseCase createUser;
  private final GetUserUseCase getUser;

  UserController(CreateUserUseCase createUser, GetUserUseCase getUser) {
    this.createUser = createUser;
    this.getUser = getUser;
  }

  @PostMapping
  ResponseEntity<CreateUserResponse> create(@RequestBody CreateUserRequest request) {
    Long id = createUser.create(request.toCommand());
    return ResponseEntity.created(URI.create("/users/" + id)).body(new CreateUserResponse(id));
  }

  @GetMapping("/{id}")
  UserResponse get(@PathVariable Long id) {
    return UserResponse.from(getUser.get(id));
  }
}
