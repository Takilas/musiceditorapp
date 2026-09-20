package com.perebziak.musiceditor.mapper;

import com.perebziak.musiceditor.dto.UserDTO;
import com.perebziak.musiceditor.model.User;

public class UserMapper {

  private UserMapper() {
  }

  public static UserDTO toDTO(User user) {
    if (user == null) return null;
    UserDTO dto = new UserDTO();
    dto.setId(user.getId());
    dto.setUsername(user.getUsername());
    dto.setEmail(user.getEmail());
    dto.setRole(user.getRole());
    dto.setRegistrationDate(user.getRegistrationDate());
    return dto;
  }
}