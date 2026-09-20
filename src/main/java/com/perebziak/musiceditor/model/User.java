package com.perebziak.musiceditor.model;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class User extends BaseEntity {
  private String username;
  private String email;
  private String passwordHash;
  private String role;              // "USER" або "ADMIN"
  private LocalDateTime registrationDate;
}