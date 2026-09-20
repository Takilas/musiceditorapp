package com.perebziak.musiceditor.controller;

import com.perebziak.musiceditor.core.AppContext;
import com.perebziak.musiceditor.core.SceneNavigator;
import com.perebziak.musiceditor.util.SettingsManager;
import com.perebziak.musiceditor.util.ThemeManager;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;

public class SettingsController {

  private final AppContext context;
  private final SceneNavigator navigator;
  private boolean initializing = true;

  @FXML private ChoiceBox<String> themeChoiceBox;

  public SettingsController(AppContext context, SceneNavigator navigator) {
    this.context = context;
    this.navigator = navigator;
  }

  @FXML
  private void initialize() {
    themeChoiceBox.setItems(FXCollections.observableArrayList("Світла", "Темна"));

    String currentTheme = SettingsManager.getInstance().getTheme();
    themeChoiceBox.getSelectionModel().select("dark".equals(currentTheme) ? "Темна" : "Світла");

    initializing = false;

    themeChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
      if (initializing || newVal == null) return;
      SettingsManager.getInstance().setTheme("Темна".equals(newVal) ? "dark" : "light");
      ThemeManager.apply(themeChoiceBox.getScene());
    });
  }

  @FXML
  private void onLogoutClick() {
    context.getPlayerService().stop();
    context.getSessionManager().logout();
    navigator.switchScene("/login-view.fxml", "Музичний редактор — Вхід",
        new LoginController(context, navigator));
  }

  @FXML
  private void onBackClick() {
    navigator.switchScene("/main-view.fxml", "Музичний редактор — Бібліотека",
        new MainController(context, navigator));
  }
}