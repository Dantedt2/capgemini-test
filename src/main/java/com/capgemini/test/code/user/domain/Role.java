package com.capgemini.test.code.user.domain;

import java.util.Arrays;

public enum Role {
  ADMIN,
  SUPERADMIN;

  public static Role from(String value) {
    return Arrays.stream(values())
        .filter(role -> role.name().equalsIgnoreCase(value))
        .findFirst()
        .orElseThrow(() -> new ValidationException("rol"));
  }
}
