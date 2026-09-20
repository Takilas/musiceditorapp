package com.perebziak.musiceditor.util;

import java.io.*;
import java.util.Properties;

public class SettingsManager {

  private static final String SETTINGS_FILE = "settings.properties";
  private static SettingsManager instance;

  private String theme;

  private SettingsManager() {
    Properties props = new Properties();
    File file = new File(SETTINGS_FILE);
    if (file.exists()) {
      try (InputStream in = new FileInputStream(file)) {
        props.load(in);
      } catch (IOException ignored) {
      }
    }
    this.theme = props.getProperty("theme", "light");
  }

  public static synchronized SettingsManager getInstance() {
    if (instance == null) instance = new SettingsManager();
    return instance;
  }

  public String getTheme() {
    return theme;
  }

  public void setTheme(String theme) {
    this.theme = theme;
    save();
  }

  private void save() {
    Properties props = new Properties();
    props.setProperty("theme", theme);
    try (OutputStream out = new FileOutputStream(SETTINGS_FILE)) {
      props.store(out, "Налаштування застосунку Музичний редактор");
    } catch (IOException e) {
      throw new RuntimeException("Не вдалося зберегти налаштування", e);
    }
  }
}