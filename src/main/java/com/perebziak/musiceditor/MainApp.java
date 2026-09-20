package com.perebziak.musiceditor;

import com.perebziak.musiceditor.controller.LoginController;
import com.perebziak.musiceditor.core.AppContext;
import com.perebziak.musiceditor.core.SceneNavigator;

import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

  @Override
  public void start(Stage primaryStage) {
    AppContext appContext = AppContext.getInstance();
    SceneNavigator navigator = SceneNavigator.getInstance();
    navigator.init(primaryStage);

    navigator.switchScene("/login-view.fxml", "Музичний редактор — Вхід",
        new LoginController(appContext, navigator));
  }

  public static void main(String[] args) {
    launch(args);
  }
}