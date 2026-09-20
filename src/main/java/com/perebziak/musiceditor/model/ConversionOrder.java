package com.perebziak.musiceditor.model;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ConversionOrder extends BaseEntity {
  private Long userId;
  private Long trackId;
  private String targetFormat;
  private BigDecimal price;
  private String status; // PENDING, PAID, COMPLETED, FAILED
  private String resultFilePath;
  private LocalDateTime createdDate;
  private LocalDateTime completedDate;
}