package com.perebziak.musiceditor.model;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class Track extends BaseEntity {
  private String title;
  private int durationSeconds;
  private String filePath;
  private Long albumId;             // FK -> albums.album_id, може бути null
  private LocalDateTime addedDate;

  // Поля версіонування (для функції редагування)
  private Long sourceTrackId;       // FK -> tracks.track_id (рекурсивний), null для оригіналу
  private AudioOperation editType;  // null для оригіналу
  private String editParams;        // наприклад "start=00:00:30,end=00:01:30" або "gain=1.5"
  private Long editedBy;            // FK -> users.user_id, хто створив цю версію
  private LocalDateTime editedDate;
}