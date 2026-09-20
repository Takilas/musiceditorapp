package com.perebziak.musiceditor.util;

import javafx.scene.control.Alert;

public class AlertHelper {

  private AlertHelper() {
  }

  public static void showError(String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Помилка");
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

  public static void showInfo(String message) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Інформація");
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

  public static boolean showConfirm(String message) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Підтвердження");
    alert.setHeaderText(null);
    alert.setContentText(message);
    return alert.showAndWait().filter(b -> b == javafx.scene.control.ButtonType.OK).isPresent();
  }
}