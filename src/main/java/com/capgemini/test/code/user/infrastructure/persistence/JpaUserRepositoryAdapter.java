package com.capgemini.test.code.user.infrastructure.persistence;

import com.capgemini.test.code.user.application.port.out.UserRepository;
import com.capgemini.test.code.user.domain.User;
import com.capgemini.test.code.user.domain.ValidationException;
import java.util.Map;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

@Repository
class JpaUserRepositoryAdapter implements UserRepository {

  private static final Map<String, String> FIELD_BY_UNIQUE_CONSTRAINT = Map.of(
      "users_email_key", "email",
      "users_dni_key", "dni",
      "users_phone_key", "phone");

  private final SpringDataUserRepository jpa;

  JpaUserRepositoryAdapter(SpringDataUserRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public User save(User user) {
    try {
      return jpa.saveAndFlush(UserEntity.from(user)).toDomain();
    } catch (DataIntegrityViolationException e) {
      String detail = String.valueOf(e.getMostSpecificCause().getMessage());
      String field = FIELD_BY_UNIQUE_CONSTRAINT.entrySet().stream()
          .filter(entry -> detail.contains(entry.getKey()))
          .map(Map.Entry::getValue)
          .findFirst()
          .orElseThrow(() -> e);
      throw new ValidationException(field);
    }
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
