package com.theo.casino;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class AppUser {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 50)
  private String username;

  @Column(nullable = false)
  private String passwordHash;

  protected AppUser() {}

  public AppUser(String username, String passwordHash) {
    this.username = username;
    this.passwordHash = passwordHash;
  }

  public Long getId() { return id; }
  public String getUsername() { return username; }
  public String getPasswordHash() { return passwordHash; }
}
