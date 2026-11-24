package com.capgemini.test.code.user.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.capgemini.test.code.user.application.port.out.UserRepository;
import com.capgemini.test.code.user.domain.Role;
import com.capgemini.test.code.user.domain.User;
import com.capgemini.test.code.user.domain.UserNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class GetUserServiceTest {

  private final UserRepository repository = mock(UserRepository.class);
  private final GetUserService service = new GetUserService(repository);

  @Test
  void returnsUserFromDefaultRoom() {
    User user = new User(1L, "pablo", "email@email.com", "677998899", Role.ADMIN, "23454234W", 1L);
    when(repository.findByIdAndRoomId(1L, Rooms.DEFAULT_ROOM_ID)).thenReturn(Optional.of(user));

    assertThat(service.get(1L)).isEqualTo(user);
  }

  @Test
  void throwsWhenUserIsNotInDefaultRoom() {
    when(repository.findByIdAndRoomId(2L, Rooms.DEFAULT_ROOM_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.get(2L))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessage("user not found");
  }
}
