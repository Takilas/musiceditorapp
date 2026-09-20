package com.perebziak.musiceditor.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
  private Long id;
  private String username;
  private String email;
  private String role;
  private LocalDateTime registrationDate;
}