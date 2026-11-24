package com.capgemini.test.code.user.application.port.in;

import com.capgemini.test.code.user.domain.User;

public interface GetUserUseCase {

  User get(Long id);
}
