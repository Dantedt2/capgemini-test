package com.capgemini.test.code.user.infrastructure.notification;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.capgemini.test.code.user.domain.Role;
import com.capgemini.test.code.user.domain.User;
import org.junit.jupiter.api.Test;

class NotifierTest {

  private static final User USER =
      new User(1L, "pablo", "email@email.com", "677998899", Role.ADMIN, "23454234W", 1L);

  private final NotificationClient client = mock(NotificationClient.class);

  @Test
  void emailNotifierSendsMessageToUserEmail() {
    new EmailNotifier(client).notify(USER, "usuario guardado");

    verify(client).sendEmail(new EmailRequest("email@email.com", "usuario guardado"));
    verifyNoMoreInteractions(client);
  }

  @Test
  void smsNotifierSendsMessageToUserPhone() {
    new SmsNotifier(client).notify(USER, "usuario guardado");

    verify(client).sendSms(new SmsRequest("677998899", "usuario guardado"));
    verifyNoMoreInteractions(client);
  }
}
