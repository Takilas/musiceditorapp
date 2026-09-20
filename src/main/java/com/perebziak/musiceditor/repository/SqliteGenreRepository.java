package com.perebziak.musiceditor.repository;

import com.perebziak.musiceditor.db.ConnectionPool;
import com.perebziak.musiceditor.model.Genre;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteGenreRepository implements GenreRepository {

  private final ConnectionPool connectionPool;

  public SqliteGenreRepository(ConnectionPool connectionPool) {
    this.connectionPool = connectionPool;
  }

  private Genre mapRow(ResultSet rs) throws SQLException {
    Genre genre = new Genre();
    genre.setId(rs.getLong("genre_id"));
    genre.setName(rs.getString("name"));
    return genre;
  }

  @Override
  public Optional<Genre> findById(Long id) {
    String sql = "SELECT * FROM genres WHERE genre_id = ?";
    Connection connection = connectionPool.getConnection();
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setLong(1, id);
      ResultSet rs = ps.executeQuery();
      return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
    } catch (SQLException e) {
      throw new RuntimeException("Помилка пошуку жанру за id=" + id, e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  @Override
  public List<Genre> findAll() {
    String sql = "SELECT * FROM genres ORDER BY name";
    Connection connection = connectionPool.getConnection();
    List<Genre> genres = new ArrayList<>();
    try (Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql)) {
      while (rs.next()) genres.add(mapRow(rs));
      return genres;
    } catch (SQLException e) {
      throw new RuntimeException("Помилка отримання списку жанрів", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  @Override
  public Genre save(Genre genre) {
    return genre.getId() == null ? insert(genre) : update(genre);
  }

  private Genre insert(Genre genre) {
    String sql = "INSERT INTO genres (name) VALUES (?)";
    Connection connection = connectionPool.getConnection();
    try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, genre.getName());
      ps.executeUpdate();
      ResultSet keys = ps.getGeneratedKeys();
      if (keys.next()) genre.setId(keys.getLong(1));
      return genre;
    } catch (SQLException e) {
      throw new RuntimeException("Помилка збереження жанру", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  private Genre update(Genre genre) {
    String sql = "UPDATE genres SET name = ? WHERE genre_id = ?";
    Connection connection = connectionPool.getConnection();
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1, genre.getName());
      ps.setLong(2, genre.getId());
      ps.executeUpdate();
      return genre;
    } catch (SQLException e) {
      throw new RuntimeException("Помилка оновлення жанру", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  @Override
  public void delete(Long id) {
    String sql = "DELETE FROM genres WHERE genre_id = ?";
    Connection connection = connectionPool.getConnection();
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setLong(1, id);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Помилка видалення жанру id=" + id, e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  @Override
  public Optional<Genre> findByName(String name) {
    String sql = "SELECT * FROM genres WHERE name = ?";
    Connection connection = connectionPool.getConnection();
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1, name);
      ResultSet rs = ps.executeQuery();
      return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
    } catch (SQLException e) {
      throw new RuntimeException("Помилка пошуку жанру за назвою", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  @Override
  public boolean existsByName(String name) {
    String sql = "SELECT 1 FROM genres WHERE name = ?";
    Connection connection = connectionPool.getConnection();
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1, name);
      return ps.executeQuery().next();
    } catch (SQLException e) {
      throw new RuntimeException("Помилка перевірки існування жанру", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }
}