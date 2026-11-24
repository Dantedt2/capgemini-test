package com.capgemini.test.code.user.domain;

public class ValidationException extends RuntimeException {

  public ValidationException(String field) {
    super("error validation " + field);
  }
}
