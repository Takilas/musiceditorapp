package com.perebziak.musiceditor.model;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class Playlist extends BaseEntity {
  private String name;
  private Long userId;              // FK -> users.user_id
  private LocalDateTime createdDate;
}