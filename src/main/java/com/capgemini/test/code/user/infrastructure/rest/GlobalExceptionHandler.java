package com.capgemini.test.code.user.infrastructure.rest;

import com.capgemini.test.code.user.domain.UserNotFoundException;
import com.capgemini.test.code.user.domain.ValidationException;
import feign.FeignException;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(ValidationException.class)
  ResponseEntity<ErrorResponse> validation(ValidationException e) {
    return error(HttpStatus.CONFLICT, e.getMessage());
  }

  @ExceptionHandler(UserNotFoundException.class)
  ResponseEntity<ErrorResponse> notFound(UserNotFoundException e) {
    return error(HttpStatus.NOT_FOUND, e.getMessage());
  }

  @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
  ResponseEntity<ErrorResponse> malformed(Exception e) {
    return error(HttpStatus.BAD_REQUEST, "malformed request");
  }

  @ExceptionHandler(FeignException.class)
  ResponseEntity<ErrorResponse> dniServiceFailure(FeignException e) {
    log.error("DNI validation service failed", e);
    return error(HttpStatus.BAD_GATEWAY, "dni validation unavailable");
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ErrorResponse> unexpected(Exception e) {
    // Framework errors (404 unknown path, 405, 415...) carry their own status; keep it instead of turning them into 500.
    if (e instanceof org.springframework.web.ErrorResponse frameworkError) {
      HttpStatus status = HttpStatus.valueOf(frameworkError.getStatusCode().value());
      return error(status, status.getReasonPhrase().toLowerCase(Locale.ROOT));
    }
    log.error("Unexpected error", e);
    return error(HttpStatus.INTERNAL_SERVER_ERROR, "internal error");
  }

  private static ResponseEntity<ErrorResponse> error(HttpStatus status, String message) {
    return ResponseEntity.status(status).body(new ErrorResponse(status.value(), message));
  }
}
