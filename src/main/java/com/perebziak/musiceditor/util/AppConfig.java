package com.perebziak.musiceditor.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {

  private final Properties properties = new Properties();

  public AppConfig() {
    try (InputStream input = getClass().getResourceAsStream("/email.properties")) {
      if (input == null) {
        throw new IllegalStateException(
            "Не знайдено email.properties у src/main/resources/. " +
                "Скопіюй email.properties.example у email.properties і заповни своїми даними.");
      }
      properties.load(input);
    } catch (IOException e) {
      throw new RuntimeException("Помилка читання email.properties", e);
    }
  }

  public String getSmtpHost() {
    return properties.getProperty("smtp.host");
  }

  public int getSmtpPort() {
    return Integer.parseInt(properties.getProperty("smtp.port"));
  }

  public String getSmtpUsername() {
    return properties.getProperty("smtp.username");
  }

  public String getSmtpAppPassword() {
    return properties.getProperty("smtp.app.password");
  }
}