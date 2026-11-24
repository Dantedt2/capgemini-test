package com.capgemini.test.code.user.infrastructure.notification;

import com.capgemini.test.code.support.IntegrationContainers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class NotificationClientIT extends IntegrationContainers {

  @Autowired
  private NotificationClient client;

  @Test
  void sendsEmailWithExpectedBody() {
    client.sendEmail(new EmailRequest("client-it@mail.com", "usuario guardado"));

    assertMockServerReceivedOnce("/email", """
        {"email": "client-it@mail.com", "message": "usuario guardado"}""");
  }

  @Test
  void sendsSmsWithExpectedBody() {
    client.sendSms(new SmsRequest("799999999", "usuario guardado"));

    assertMockServerReceivedOnce("/sms", """
        {"phone": "799999999", "message": "usuario guardado"}""");
  }
}
