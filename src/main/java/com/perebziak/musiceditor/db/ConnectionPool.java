package com.perebziak.musiceditor.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ConnectionPool {

  private static final String DB_URL = "jdbc:sqlite:musiceditor.db";
  private static final int POOL_SIZE = 4;

  private static ConnectionPool instance;
  private final BlockingQueue<Connection> pool = new ArrayBlockingQueue<>(POOL_SIZE);

  private ConnectionPool() {
    try {
      Class.forName("org.sqlite.JDBC");
      for (int i = 0; i < POOL_SIZE; i++) {
        pool.add(createConnection());
      }
    } catch (ClassNotFoundException | SQLException e) {
      throw new RuntimeException("Не вдалося ініціалізувати пул з'єднань із БД", e);
    }
  }

  public static synchronized ConnectionPool getInstance() {
    if (instance == null) {
      instance = new ConnectionPool();
    }
    return instance;
  }

  private Connection createConnection() throws SQLException {
    Connection connection = DriverManager.getConnection(DB_URL);
    // SQLite за замовчуванням ВИМИКАЄ перевірку зовнішніх ключів
    // на кожному з'єднанні окремо — обов'язково вмикаємо вручну.
    try (Statement statement = connection.createStatement()) {
      statement.execute("PRAGMA foreign_keys = ON;");
    }
    return connection;
  }

  public Connection getConnection() {
    try {
      return pool.take();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Очікування вільного з'єднання перервано", e);
    }
  }

  public void releaseConnection(Connection connection) {
    if (connection != null) {
      pool.offer(connection);
    }
  }
}