package com.capgemini.test.code.user.domain;

public record User(Long id, String name, String email, String phone, Role role, String dni, Long roomId) {
}
