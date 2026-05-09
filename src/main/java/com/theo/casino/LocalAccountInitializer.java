package com.theo.casino;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class LocalAccountInitializer {

  @Bean
  CommandLineRunner createLocalAccount(UserRepository users, PasswordEncoder encoder) {
    return args -> {
      if (!users.existsByUsername("theo")) {
        users.save(new AppUser("theo", encoder.encode("casino123")));
      }
    };
  }
}
