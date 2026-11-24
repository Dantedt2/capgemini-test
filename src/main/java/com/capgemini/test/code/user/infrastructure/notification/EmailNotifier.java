package com.capgemini.test.code.user.infrastructure.notification;

import com.capgemini.test.code.user.application.port.out.UserNotifier;
import com.capgemini.test.code.user.domain.User;

public class EmailNotifier implements UserNotifier {

  private final NotificationClient client;

  public EmailNotifier(NotificationClient client) {
    this.client = client;
  }

  @Override
  public void notify(User user, String message) {
    client.sendEmail(new EmailRequest(user.email(), message));
  }
}
