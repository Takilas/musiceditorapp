package com.perebziak.musiceditor.service;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.File;
import java.util.function.Consumer;

public class PlayerService {

  private MediaPlayer currentPlayer;

  public void play(String filePath, Consumer<Duration> onDurationReady, Consumer<String> onError) {
    stop();
    System.out.println("[PlayerService] Спроба відтворити: " + filePath);
    try {
      File file = new File(filePath);
      System.out.println("[PlayerService] Абсолютний шлях: " + file.getAbsolutePath());
      System.out.println("[PlayerService] Файл існує: " + file.exists());

      if (!file.exists()) {
        onError.accept("Файл не знайдено на диску: " + file.getAbsolutePath());
        return;
      }

      Media media = new Media(file.toURI().toString());
      System.out.println("[PlayerService] Media створено, URI: " + file.toURI());

      currentPlayer = new MediaPlayer(media);
      System.out.println("[PlayerService] MediaPlayer створено, статус: " + currentPlayer.getStatus());

      currentPlayer.setOnReady(() -> {
        System.out.println("[PlayerService] onReady спрацював!");
        if (onDurationReady != null) {
          onDurationReady.accept(currentPlayer.getTotalDuration());
        }
      });

      currentPlayer.setOnError(() -> {
        System.out.println("[PlayerService] onError спрацював!");
        String message = currentPlayer.getError() != null
            ? currentPlayer.getError().getMessage()
            : "Невідома помилка відтворення";
        System.out.println("[PlayerService] Текст помилки: " + message);
        if (onError != null) {
          onError.accept(message);
        }
      });

      currentPlayer.setOnEndOfMedia(this::stop);
      currentPlayer.play();
      System.out.println("[PlayerService] play() викликано");
    } catch (Exception e) {
      System.out.println("[PlayerService] Виняток: " + e);
      e.printStackTrace();
      if (onError != null) {
        onError.accept(e.getMessage());
      }
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