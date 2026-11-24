package com.capgemini.test.code.user.domain;

public class UserNotFoundException extends RuntimeException {

  public UserNotFoundException() {
    super("user not found");
  }
}
