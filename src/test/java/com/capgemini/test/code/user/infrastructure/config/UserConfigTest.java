package com.capgemini.test.code.user.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.capgemini.test.code.user.application.port.out.UserNotifier;
import com.capgemini.test.code.user.domain.Role;
import com.capgemini.test.code.user.infrastructure.notification.EmailNotifier;
import com.capgemini.test.code.user.infrastructure.notification.NotificationClient;
import com.capgemini.test.code.user.infrastructure.notification.SmsNotifier;
import java.util.Map;
import org.junit.jupiter.api.Test;

class UserConfigTest {

  private final Map<Role, UserNotifier> notifiers =
      new UserConfig().notifiersByRole(mock(NotificationClient.class));

  @Test
  void everyRoleHasANotifier() {
    assertThat(notifiers).containsOnlyKeys(Role.values());
  }

  @Test
  void adminIsNotifiedByEmailAndSuperadminBySms() {
    assertThat(notifiers.get(Role.ADMIN)).isInstanceOf(EmailNotifier.class);
    assertThat(notifiers.get(Role.SUPERADMIN)).isInstanceOf(SmsNotifier.class);
  }
}
