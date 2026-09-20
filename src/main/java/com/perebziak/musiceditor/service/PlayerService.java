package com.perebziak.musiceditor.service;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.File;
import java.util.function.Consumer;

public class PlayerService {

  private MediaPlayer currentPlayer;

  private final ObjectProperty<Duration> currentTime = new SimpleObjectProperty<>(Duration.ZERO);
  private final ObjectProperty<Duration> totalDuration = new SimpleObjectProperty<>(Duration.ZERO);

  public void play(String filePath, Consumer<Duration> onDurationReady, Consumer<String> onError) {
    stop();
    try {
      File file = new File(filePath);
      if (!file.exists()) {
        if (onError != null) onError.accept("Файл не знайдено на диску: " + file.getAbsolutePath());
        return;
      }

      Media media = new Media(file.toURI().toString());
      currentPlayer = new MediaPlayer(media);

      currentPlayer.currentTimeProperty().addListener((obs, oldT, newT) -> currentTime.set(newT));

      currentPlayer.setOnReady(() -> {
        totalDuration.set(currentPlayer.getTotalDuration());
        if (onDurationReady != null) {
          onDurationReady.accept(currentPlayer.getTotalDuration());
        }
      });

      currentPlayer.setOnError(() -> {
        String message = currentPlayer.getError() != null
            ? currentPlayer.getError().getMessage()
            : "Невідома помилка відтворення";
        if (onError != null) {
          onError.accept(message);
        }
      });

      currentPlayer.setOnEndOfMedia(this::stop);
      currentPlayer.play();
    } catch (Exception e) {
      if (onError != null) {
        onError.accept(e.getMessage());
      }
    }
  }

  public void seek(Duration position) {
    if (currentPlayer != null) {
      currentPlayer.seek(position);
    }
  }

  public void stop() {
    if (currentPlayer != null) {
      currentPlayer.stop();
      currentPlayer.dispose();
      currentPlayer = null;
    }
    currentTime.set(Duration.ZERO);
    totalDuration.set(Duration.ZERO);
  }

  public boolean isPlaying() {
    return currentPlayer != null
        && currentPlayer.getStatus() == MediaPlayer.Status.PLAYING;
  }

  public ObjectProperty<Duration> currentTimeProperty() {
    return currentTime;
  }

  public ObjectProperty<Duration> totalDurationProperty() {
    return totalDuration;
  }
}