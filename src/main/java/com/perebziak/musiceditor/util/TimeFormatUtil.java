package com.perebziak.musiceditor.util;

import javafx.util.Duration;

public class TimeFormatUtil {

  private TimeFormatUtil() {
  }

  public static String format(Duration duration) {
    if (duration == null || duration.isUnknown()) {
      return "00:00";
    }
    int totalSeconds = (int) duration.toSeconds();
    int minutes = totalSeconds / 60;
    int seconds = totalSeconds % 60;
    return String.format("%02d:%02d", minutes, seconds);
  }
}