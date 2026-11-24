package com.capgemini.test.code.user.infrastructure.dni;

import static org.assertj.core.api.Assertions.assertThat;

import com.capgemini.test.code.support.IntegrationContainers;
import com.capgemini.test.code.user.application.port.out.DniValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class FeignDniValidatorIT extends IntegrationContainers {

  @Autowired
  private DniValidator dniValidator;

  @Test
  void acceptsDniApprovedByExternalService() {
    assertThat(dniValidator.isValid("23454234W")).isTrue();
  }

  @Test
  void rejectsDniRefusedByExternalService() {
    assertThat(dniValidator.isValid("99999999w")).isFalse();
  }
}
