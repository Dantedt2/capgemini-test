package com.capgemini.test.code.user.infrastructure.rest;

import com.capgemini.test.code.user.domain.User;
import java.util.Locale;

record UserResponse(Long id, String name, String email, String phone, String rol, String dni) {

  static UserResponse from(User user) {
    return new UserResponse(user.id(), user.name(), user.email(), user.phone(),
        user.role().name().toLowerCase(Locale.ROOT), user.dni());
  }
}
