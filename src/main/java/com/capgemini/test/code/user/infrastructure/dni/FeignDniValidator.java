package com.capgemini.test.code.user.infrastructure.dni;

import com.capgemini.test.code.user.application.port.out.DniValidator;
import feign.FeignException;
import org.springframework.stereotype.Component;

@Component
class FeignDniValidator implements DniValidator {

  private final DniClient client;

  FeignDniValidator(DniClient client) {
    this.client = client;
  }

  @Override
  public boolean isValid(String dni) {
    try {
      client.check(new CheckDniRequest(dni));
      return true;
    } catch (FeignException.Conflict e) {
      return false;
    }
  }
}
