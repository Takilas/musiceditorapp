package com.perebziak.musiceditor.model;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class VerificationCode extends BaseEntity {
  private String email;
  private String code;
  private LocalDateTime createdDate;
  private LocalDateTime expiresDate;
  private boolean used;
}