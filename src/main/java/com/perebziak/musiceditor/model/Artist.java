package com.perebziak.musiceditor.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Artist extends BaseEntity {
  private String name;
  private String country;
}