package com.perebziak.musiceditor.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Album extends BaseEntity {
  private String title;
  private Integer releaseYear;
  private Long artistId;   // FK -> artists.artist_id
}