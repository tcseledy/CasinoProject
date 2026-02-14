package com.theo.casino.security;

import com.theo.casino.UserRepository;
import com.theo.casino.AppUser;

import java.util.Collections;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class DbUserDetailsService implements UserDetailsService {
  private final UserRepository users;

  public DbUserDetailsService(UserRepository users) {
    this.users = users;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    AppUser u = users.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    return new org.springframework.security.core.userdetails.User(
        u.getUsername(),
        u.getPasswordHash(),
        Collections.emptyList()
    );
  }
}
