package com.capgemini.test.code.user.infrastructure.persistence;

import com.capgemini.test.code.user.domain.Role;
import com.capgemini.test.code.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
class UserEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String name;
  private String email;
  private String phone;
  private String dni;
  @Enumerated(EnumType.STRING)
  private Role role;
  @Column(name = "room_id")
  private Long roomId;

  protected UserEntity() {
  }

  static UserEntity from(User user) {
    UserEntity entity = new UserEntity();
    entity.id = user.id();
    entity.name = user.name();
    entity.email = user.email();
    entity.phone = user.phone();
    entity.dni = user.dni();
    entity.role = user.role();
    entity.roomId = user.roomId();
    return entity;
  }

  User toDomain() {
    return new User(id, name, email, phone, role, dni, roomId);
  }
}
