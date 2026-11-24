package com.capgemini.test.code.user.application.service;

import com.capgemini.test.code.user.application.port.in.GetUserUseCase;
import com.capgemini.test.code.user.application.port.out.UserRepository;
import com.capgemini.test.code.user.domain.User;
import com.capgemini.test.code.user.domain.UserNotFoundException;

public class GetUserService implements GetUserUseCase {

  private final UserRepository repository;

  public GetUserService(UserRepository repository) {
    this.repository = repository;
  }

  @Override
  public User get(Long id) {
    return repository.findByIdAndRoomId(id, Rooms.DEFAULT_ROOM_ID).orElseThrow(UserNotFoundException::new);
  }
}
