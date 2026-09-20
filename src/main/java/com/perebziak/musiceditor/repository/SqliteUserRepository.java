package com.perebziak.musiceditor.repository;

import com.perebziak.musiceditor.db.ConnectionPool;
import com.perebziak.musiceditor.model.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class SqliteUserRepository implements UserRepository {

  private final ConnectionPool connectionPool;

  public SqliteUserRepository(ConnectionPool connectionPool) {
    this.connectionPool = connectionPool;
  }

  // ----------------------------------------------------------------
  //  Маппінг ResultSet -> User (патерн Data Mapper, методичка 5.3)
  // ----------------------------------------------------------------
  private User mapRow(ResultSet rs) throws SQLException {
    User user = new User();
    user.setId(rs.getLong("user_id"));
    user.setUsername(rs.getString("username"));
    user.setEmail(rs.getString("email"));
    user.setPasswordHash(rs.getString("password_hash"));
    user.setRole(rs.getString("role"));
    String regDate = rs.getString("registration_date");
    if (regDate != null) {
      user.setRegistrationDate(LocalDateTime.parse(regDate.replace(" ", "T")));
    }
    return user;
  }

  // ----------------------------------------------------------------
  //  CRUD
  // ----------------------------------------------------------------
  @Override
  public Optional<User> findById(Long id) {
    String sql = "SELECT * FROM users WHERE user_id = ?";
    Connection connection = connectionPool.getConnection();
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setLong(1, id);
      ResultSet rs = ps.executeQuery();
      return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
    } catch (SQLException e) {
      throw new RuntimeException("Помилка пошуку користувача за id=" + id, e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  @Override
  public List<User> findAll() {
    String sql = "SELECT * FROM users";
    Connection connection = connectionPool.getConnection();
    List<User> users = new ArrayList<>();
    try (Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql)) {
      while (rs.next()) users.add(mapRow(rs));
      return users;
    } catch (SQLException e) {
      throw new RuntimeException("Помилка отримання списку користувачів", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  @Override
  public User save(User user) {
    if (user.getId() == null) {
      return insert(user);
    } else {
      return update(user);
    }
  }

  private User insert(User user) {
    String sql = """
                INSERT INTO users (username, email, password_hash, role)
                VALUES (?, ?, ?, ?)
                """;
    Connection connection = connectionPool.getConnection();
    try (PreparedStatement ps = connection.prepareStatement(
        sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, user.getUsername());
      ps.setString(2, user.getEmail());
      ps.setString(3, user.getPasswordHash());
      ps.setString(4, user.getRole() != null ? user.getRole() : "USER");
      ps.executeUpdate();
      ResultSet keys = ps.getGeneratedKeys();
      if (keys.next()) user.setId(keys.getLong(1));
      return user;
    } catch (SQLException e) {
      throw new RuntimeException("Помилка збереження користувача", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  private User update(User user) {
    String sql = """
                UPDATE users SET username=?, email=?, password_hash=?, role=?
                WHERE user_id=?
                """;
    Connection connection = connectionPool.getConnection();
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1, user.getUsername());
      ps.setString(2, user.getEmail());
      ps.setString(3, user.getPasswordHash());
      ps.setString(4, user.getRole());
      ps.setLong(5, user.getId());
      ps.executeUpdate();
      return user;
    } catch (SQLException e) {
      throw new RuntimeException("Помилка оновлення користувача", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  @Override
  public void delete(Long id) {
    String sql = "DELETE FROM users WHERE user_id = ?";
    Connection connection = connectionPool.getConnection();
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setLong(1, id);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Помилка видалення користувача id=" + id, e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  // ----------------------------------------------------------------
  //  Додаткові методи (специфічні для User)
  // ----------------------------------------------------------------
  @Override
  public Optional<User> findByEmail(String email) {
    String sql = "SELECT * FROM users WHERE email = ?";
    Connection connection = connectionPool.getConnection();
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1, email);
      ResultSet rs = ps.executeQuery();
      return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
    } catch (SQLException e) {
      throw new RuntimeException("Помилка пошуку користувача за email", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  @Override
  public Optional<User> findByUsername(String username) {
    String sql = "SELECT * FROM users WHERE username = ?";
    Connection connection = connectionPool.getConnection();
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1, username);
      ResultSet rs = ps.executeQuery();
      return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
    } catch (SQLException e) {
      throw new RuntimeException("Помилка пошуку користувача за username", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  @Override
  public boolean existsByEmail(String email) {
    String sql = "SELECT 1 FROM users WHERE email = ?";
    Connection connection = connectionPool.getConnection();
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1, email);
      return ps.executeQuery().next();
    } catch (SQLException e) {
      throw new RuntimeException("Помилка перевірки email", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  @Override
  public boolean existsByUsername(String username) {
    String sql = "SELECT 1 FROM users WHERE username = ?";
    Connection connection = connectionPool.getConnection();
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1, username);
      return ps.executeQuery().next();
    } catch (SQLException e) {
      throw new RuntimeException("Помилка перевірки username", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }
}