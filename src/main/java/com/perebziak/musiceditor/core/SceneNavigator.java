package com.perebziak.musiceditor.core;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class SceneNavigator {

  private static SceneNavigator instance;
  private Stage primaryStage;

  private SceneNavigator() {
  }

  public static synchronized SceneNavigator getInstance() {
    if (instance == null) {
      instance = new SceneNavigator();
    }
    return instance;
  }

  public void init(Stage primaryStage) {
    this.primaryStage = primaryStage;
  }

  public <T> T switchScene(String fxmlPath, String title, T controller) {
    try {
      URL url = getClass().getResource(fxmlPath);
      if (url == null) {
        throw new IllegalStateException("Не знайдено FXML-файл: " + fxmlPath);
      }
      FXMLLoader loader = new FXMLLoader(url);
      loader.setController(controller);
      Parent root = loader.load();

      Scene scene = new Scene(root);
      com.perebziak.musiceditor.util.ThemeManager.apply(scene);
      primaryStage.setScene(scene);
      primaryStage.setTitle(title);
      primaryStage.show();

      return controller;
    } catch (IOException e) {
      throw new RuntimeException("Помилка завантаження екрану " + fxmlPath, e);
    }
  }
}