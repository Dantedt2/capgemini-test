package com.capgemini.test.code.user.infrastructure.config;

import com.capgemini.test.code.user.application.port.in.CreateUserUseCase;
import com.capgemini.test.code.user.application.port.in.GetUserUseCase;
import com.capgemini.test.code.user.application.port.out.DniValidator;
import com.capgemini.test.code.user.application.port.out.UserNotifier;
import com.capgemini.test.code.user.application.port.out.UserRepository;
import com.capgemini.test.code.user.application.service.CreateUserService;
import com.capgemini.test.code.user.application.service.GetUserService;
import com.capgemini.test.code.user.domain.Role;
import com.capgemini.test.code.user.infrastructure.notification.EmailNotifier;
import com.capgemini.test.code.user.infrastructure.notification.NotificationClient;
import com.capgemini.test.code.user.infrastructure.notification.SmsNotifier;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserConfig {

  @Bean
  Map<Role, UserNotifier> notifiersByRole(NotificationClient client) {
    Map<Role, UserNotifier> notifiers = new EnumMap<>(Role.class);
    notifiers.put(Role.ADMIN, new EmailNotifier(client));
    notifiers.put(Role.SUPERADMIN, new SmsNotifier(client));
    return notifiers;
  }

  @Bean
  CreateUserUseCase createUserUseCase(UserRepository repository, DniValidator dniValidator,
      Map<Role, UserNotifier> notifiersByRole) {
    return new CreateUserService(repository, dniValidator, notifiersByRole);
  }

  @Bean
  GetUserUseCase getUserUseCase(UserRepository repository) {
    return new GetUserService(repository);
  }
}
