package com.perebziak.musiceditor.repository;

import com.perebziak.musiceditor.db.ConnectionPool;
import com.perebziak.musiceditor.model.ConversionOrder;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteConversionOrderRepository implements ConversionOrderRepository {

  private static final DateTimeFormatter SQLITE_DATETIME =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private final ConnectionPool connectionPool;

  public SqliteConversionOrderRepository(ConnectionPool connectionPool) {
    this.connectionPool = connectionPool;
  }

  private ConversionOrder mapRow(ResultSet rs) throws SQLException {
    ConversionOrder order = new ConversionOrder();
    order.setId(rs.getLong("order_id"));
    order.setUserId(rs.getLong("user_id"));
    order.setTrackId(rs.getLong("track_id"));
    order.setTargetFormat(rs.getString("target_format"));
    order.setPrice(rs.getBigDecimal("price"));
    order.setStatus(rs.getString("status"));
    order.setResultFilePath(rs.getString("result_file_path"));

    String created = rs.getString("created_date");
    if (created != null) order.setCreatedDate(LocalDateTime.parse(created.replace(" ", "T")));

    String completed = rs.getString("completed_date");
    if (completed != null) order.setCompletedDate(LocalDateTime.parse(completed.replace(" ", "T")));

    return order;
  }

  @Override
  public Optional<ConversionOrder> findById(Long id) {
    String sql = "SELECT * FROM conversion_orders WHERE order_id = ?";
    Connection connection = connectionPool.getConnection();
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setLong(1, id);
      ResultSet rs = ps.executeQuery();
      return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
    } catch (SQLException e) {
      throw new RuntimeException("Помилка пошуку замовлення за id=" + id, e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  @Override
  public List<ConversionOrder> findAll() {
    String sql = "SELECT * FROM conversion_orders ORDER BY created_date DESC";
    Connection connection = connectionPool.getConnection();
    List<ConversionOrder> orders = new ArrayList<>();
    try (Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql)) {
      while (rs.next()) orders.add(mapRow(rs));
      return orders;
    } catch (SQLException e) {
      throw new RuntimeException("Помилка отримання списку замовлень", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  @Override
  public ConversionOrder save(ConversionOrder order) {
    return order.getId() == null ? insert(order) : update(order);
  }

  private ConversionOrder insert(ConversionOrder order) {
    String sql = """
        INSERT INTO conversion_orders (user_id, track_id, target_format, price, status)
        VALUES (?, ?, ?, ?, ?)
        """;
    Connection connection = connectionPool.getConnection();
    try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setLong(1, order.getUserId());
      ps.setLong(2, order.getTrackId());
      ps.setString(3, order.getTargetFormat());
      ps.setBigDecimal(4, order.getPrice());
      ps.setString(5, order.getStatus());
      ps.executeUpdate();
      ResultSet keys = ps.getGeneratedKeys();
      if (keys.next()) order.setId(keys.getLong(1));
      return order;
    } catch (SQLException e) {
      throw new RuntimeException("Помилка збереження замовлення", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  private ConversionOrder update(ConversionOrder order) {
    String sql = """
        UPDATE conversion_orders
        SET status = ?, result_file_path = ?, completed_date = ?
        WHERE order_id = ?
        """;
    Connection connection = connectionPool.getConnection();
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1, order.getStatus());
      ps.setString(2, order.getResultFilePath());
      ps.setString(3, order.getCompletedDate() != null
          ? order.getCompletedDate().format(SQLITE_DATETIME) : null);
      ps.setLong(4, order.getId());
      ps.executeUpdate();
      return order;
    } catch (SQLException e) {
      throw new RuntimeException("Помилка оновлення замовлення", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  @Override
  public void delete(Long id) {
    String sql = "DELETE FROM conversion_orders WHERE order_id = ?";
    Connection connection = connectionPool.getConnection();
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setLong(1, id);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Помилка видалення замовлення id=" + id, e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  @Override
  public List<ConversionOrder> findByUserId(Long userId) {
    String sql = "SELECT * FROM conversion_orders WHERE user_id = ? ORDER BY created_date DESC";
    Connection connection = connectionPool.getConnection();
    List<ConversionOrder> orders = new ArrayList<>();
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setLong(1, userId);
      ResultSet rs = ps.executeQuery();
      while (rs.next()) orders.add(mapRow(rs));
      return orders;
    } catch (SQLException e) {
      throw new RuntimeException("Помилка отримання замовлень користувача", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }
}