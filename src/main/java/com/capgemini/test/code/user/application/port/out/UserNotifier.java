package com.capgemini.test.code.user.application.port.out;

import com.capgemini.test.code.user.domain.User;

public interface UserNotifier {

  void notify(User user, String message);
}
