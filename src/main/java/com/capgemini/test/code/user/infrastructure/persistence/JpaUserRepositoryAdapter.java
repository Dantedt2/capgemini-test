package com.capgemini.test.code.user.infrastructure.persistence;

import com.capgemini.test.code.user.application.port.out.UserRepository;
import com.capgemini.test.code.user.domain.User;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
class JpaUserRepositoryAdapter implements UserRepository {

  private final SpringDataUserRepository jpa;

  JpaUserRepositoryAdapter(SpringDataUserRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public User save(User user) {
    return jpa.save(UserEntity.from(user)).toDomain();
  }

  @Override
  public boolean existsByEmail(String email) {
    return jpa.existsByEmail(email);
  }

  @Override
  public Optional<User> findByIdAndRoomId(Long id, Long roomId) {
    return jpa.findByIdAndRoomId(id, roomId).map(UserEntity::toDomain);
  }
}
