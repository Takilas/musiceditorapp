package com.perebziak.musiceditor.repository;

import com.perebziak.musiceditor.db.ConnectionPool;
import com.perebziak.musiceditor.model.VerificationCode;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteVerificationCodeRepository implements VerificationCodeRepository {

  private static final DateTimeFormatter SQLITE_DATETIME =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private final ConnectionPool connectionPool;

  public SqliteVerificationCodeRepository(ConnectionPool connectionPool) {
    this.connectionPool = connectionPool;
  }

  private VerificationCode mapRow(ResultSet rs) throws SQLException {
    VerificationCode vc = new VerificationCode();
    vc.setId(rs.getLong("verification_id"));
    vc.setEmail(rs.getString("email"));
    vc.setCode(rs.getString("code"));
    vc.setUsed(rs.getInt("is_used") == 1);
    String created = rs.getString("created_date");
    String expires = rs.getString("expires_date");
    if (created != null) vc.setCreatedDate(LocalDateTime.parse(created.replace(" ", "T")));
    if (expires != null) vc.setExpiresDate(LocalDateTime.parse(expires.replace(" ", "T")));
    return vc;
  }

  @Override
  public Optional<VerificationCode> findById(Long id) {
    String sql = "SELECT * FROM registration_verifications WHERE verification_id = ?";
    Connection connection = connectionPool.getConnection();
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setLong(1, id);
      ResultSet rs = ps.executeQuery();
      return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
    } catch (SQLException e) {
      throw new RuntimeException("Помилка пошуку коду верифікації", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  @Override
  public List<VerificationCode> findAll() {
    String sql = "SELECT * FROM registration_verifications";
    Connection connection = connectionPool.getConnection();
    List<VerificationCode> list = new ArrayList<>();
    try (Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql)) {
      while (rs.next()) list.add(mapRow(rs));
      return list;
    } catch (SQLException e) {
      throw new RuntimeException("Помилка отримання кодів верифікації", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  @Override
  public VerificationCode save(VerificationCode vc) {
    String sql = """
                INSERT INTO registration_verifications (email, code, expires_date, is_used)
                VALUES (?, ?, ?, 0)
                """;
    Connection connection = connectionPool.getConnection();
    try (PreparedStatement ps = connection.prepareStatement(
        sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, vc.getEmail());
      ps.setString(2, vc.getCode());
      ps.setString(3, vc.getExpiresDate().format(SQLITE_DATETIME));
      ps.executeUpdate();
      ResultSet keys = ps.getGeneratedKeys();
      if (keys.next()) vc.setId(keys.getLong(1));
      return vc;
    } catch (SQLException e) {
      throw new RuntimeException("Помилка збереження коду верифікації", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  @Override
  public void delete(Long id) {
    String sql = "DELETE FROM registration_verifications WHERE verification_id = ?";
    Connection connection = connectionPool.getConnection();
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setLong(1, id);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Помилка видалення коду верифікації", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  @Override
  public Optional<VerificationCode> findActiveByEmail(String email) {
    // Шукаємо не використаний код, термін якого ще не минув
    String sql = """
                SELECT * FROM registration_verifications
                WHERE email = ? AND is_used = 0
                  AND expires_date > datetime('now')
                ORDER BY created_date DESC
                LIMIT 1
                """;
    Connection connection = connectionPool.getConnection();
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1, email);
      ResultSet rs = ps.executeQuery();
      return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
    } catch (SQLException e) {
      throw new RuntimeException("Помилка пошуку активного коду", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  @Override
  public void markAsUsed(Long id) {
    String sql = "UPDATE registration_verifications SET is_used = 1 WHERE verification_id = ?";
    Connection connection = connectionPool.getConnection();
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setLong(1, id);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Помилка позначення коду як використаного", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  @Override
  public void deleteExpired() {
    String sql = "DELETE FROM registration_verifications WHERE expires_date < datetime('now')";
    Connection connection = connectionPool.getConnection();
    try (Statement st = connection.createStatement()) {
      st.executeUpdate(sql);
    } catch (SQLException e) {
      throw new RuntimeException("Помилка видалення прострочених кодів", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }
}