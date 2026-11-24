package com.capgemini.test.code.user.application.port.out;

import com.capgemini.test.code.user.domain.User;
import java.util.Optional;

public interface UserRepository {

  User save(User user);

  boolean existsByEmail(String email);

  Optional<User> findByIdAndRoomId(Long id, Long roomId);
}
