package com.perebziak.musiceditor.service;

import com.perebziak.musiceditor.exception.AudioProcessingException;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.File;
import java.util.function.Consumer;

public class PlayerService {

  private MediaPlayer currentPlayer;

  public void play(String filePath, Consumer<Duration> onDurationReady) {
    stop();
    try {
      File file = new File(filePath);
      Media media = new Media(file.toURI().toString());
      currentPlayer = new MediaPlayer(media);

      currentPlayer.setOnReady(() -> {
        if (onDurationReady != null) {
          onDurationReady.accept(currentPlayer.getTotalDuration());
        }
      });
      currentPlayer.setOnEndOfMedia(this::stop);
      currentPlayer.play();
    } catch (Exception e) {
      throw new AudioProcessingException("Не вдалося відтворити файл: " + filePath, e);
    }
  }

  public void stop() {
    if (currentPlayer != null) {
      currentPlayer.stop();
      currentPlayer.dispose();
      currentPlayer = null;
    }
  }

  public boolean isPlaying() {
    return currentPlayer != null
        && currentPlayer.getStatus() == MediaPlayer.Status.PLAYING;
  }
}