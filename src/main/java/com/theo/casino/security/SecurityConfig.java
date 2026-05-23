package com.theo.casino.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
  http
    .csrf(AbstractHttpConfigurer::disable)
    .authorizeHttpRequests(auth -> auth
      .requestMatchers(
        "/", "/log-in", "/signup", "/sign-up",
        "/error",
        "/css/**", "/js/**", "/images/**",
        "/h2-console/**"
      ).permitAll()
      .anyRequest().authenticated()
    )
    .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
    .formLogin(form -> form
      .loginPage("/log-in")
      .loginProcessingUrl("/log-in")
      .defaultSuccessUrl("/", true)
      .failureUrl("/log-in?error=true")
      .permitAll()
    )
    .logout(logout -> logout
      .logoutUrl("/logout")
      .logoutSuccessUrl("/log-in?logout=true")
      .permitAll()
    );

  return http.build();
    }
}
