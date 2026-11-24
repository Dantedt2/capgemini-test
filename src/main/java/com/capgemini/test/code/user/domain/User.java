package com.capgemini.test.code.user.domain;

public record User(Long id, String name, String email, String phone, Role role, String dni, Long roomId) {

  private static final int MAX_NAME_LENGTH = 6;

  public static User create(String name, String email, String phone, String rol, String dni, Long roomId) {
    if (name == null || name.isBlank() || name.length() > MAX_NAME_LENGTH) {
      throw new ValidationException("userName");
    }
    if (email == null || !email.contains("@") || !email.contains(".")) {
      throw new ValidationException("email");
    }
    Role role = Role.from(rol);
    if (phone == null || phone.isBlank()) {
      throw new ValidationException("phone");
    }
    if (dni == null || dni.isBlank()) {
      throw new ValidationException("dni");
    }
    return new User(null, name, email, phone, role, dni, roomId);
  }
}
