package com.perebziak.musiceditor;

import com.perebziak.musiceditor.db.ConnectionPool;
import com.perebziak.musiceditor.db.DatabaseManager;

public class Launcher {
  public static void main(String[] args) {
    DatabaseManager databaseManager = new DatabaseManager(ConnectionPool.getInstance());
    databaseManager.initializeIfNeeded();
    System.out.println("Перевірка завершена — дивись, чи з'явився файл musiceditor.db у корені проєкту.");
  }
}