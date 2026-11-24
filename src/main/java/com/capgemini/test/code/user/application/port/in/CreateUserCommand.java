package com.capgemini.test.code.user.application.port.in;

public record CreateUserCommand(String name, String email, String phone, String rol, String dni) {
}
