package com.perebziak.musiceditor.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

  private final ConnectionPool connectionPool;

  public DatabaseManager(ConnectionPool connectionPool) {
    this.connectionPool = connectionPool;
  }

  public void initializeIfNeeded() {
    Connection connection = connectionPool.getConnection();
    try {
      if (!tablesExist(connection)) {
        executeScript(connection, "/schema.sql");
        executeScript(connection, "/seed.sql");
        System.out.println("БД ініціалізована: схему створено, тестові дані додано.");
      } else {
        System.out.println("БД вже існує, ініціалізацію пропущено.");
      }
    } catch (SQLException e) {
      throw new RuntimeException("Помилка ініціалізації бази даних", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  private boolean tablesExist(Connection connection) throws SQLException {
    try (Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name='users';")) {
      return resultSet.next();
    }
  }

  private void executeScript(Connection connection, String resourcePath) {
    String script = readResource(resourcePath);
    String[] statements = script.split(";\\s*\\n");

    try (Statement statement = connection.createStatement()) {
      for (String rawStatement : statements) {
        String sql = rawStatement.trim();
        if (sql.isEmpty()) {
          continue;
        }
        statement.execute(sql);
      }
    } catch (SQLException e) {
      throw new RuntimeException("Помилка виконання скрипта " + resourcePath, e);
    }
  }

  private String readResource(String path) {
    try (InputStream inputStream = getClass().getResourceAsStream(path)) {
      if (inputStream == null) {
        throw new IllegalStateException("Не знайдено ресурс: " + path
            + " (перевір, що файл лежить у src/main/resources/)");
      }
      StringBuilder builder = new StringBuilder();
      try (BufferedReader reader = new BufferedReader(
          new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
        String line;
        while ((line = reader.readLine()) != null) {
          builder.append(line).append("\n");
        }
      }
      return builder.toString();
    } catch (IOException e) {
      throw new RuntimeException("Не вдалося прочитати ресурс " + path, e);
    }
  }
}