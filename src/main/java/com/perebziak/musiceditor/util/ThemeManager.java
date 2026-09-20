package com.perebziak.musiceditor.util;

import javafx.scene.Scene;

public class ThemeManager {

  private ThemeManager() {
  }

  public static void apply(Scene scene) {
    scene.getStylesheets().clear();
    scene.getStylesheets().add(ThemeManager.class.getResource("/css/common.css").toExternalForm());
    String theme = SettingsManager.getInstance().getTheme();
    String themeFile = "dark".equals(theme) ? "/css/dark.css" : "/css/light.css";
    scene.getStylesheets().add(ThemeManager.class.getResource(themeFile).toExternalForm());
  }
}