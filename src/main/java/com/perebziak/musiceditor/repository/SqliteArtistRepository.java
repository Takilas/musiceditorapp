package com.perebziak.musiceditor.repository;

import com.perebziak.musiceditor.db.ConnectionPool;
import com.perebziak.musiceditor.model.Artist;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteArtistRepository implements ArtistRepository {

  private final ConnectionPool connectionPool;

  public SqliteArtistRepository(ConnectionPool connectionPool) {
    this.connectionPool = connectionPool;
  }

  private Artist mapRow(ResultSet rs) throws SQLException {
    Artist artist = new Artist();
    artist.setId(rs.getLong("artist_id"));
    artist.setName(rs.getString("name"));
    artist.setCountry(rs.getString("country"));
    return artist;
  }

  @Override
  public Optional<Artist> findById(Long id) {
    String sql = "SELECT * FROM artists WHERE artist_id = ?";
    Connection connection = connectionPool.getConnection();
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setLong(1, id);
      ResultSet rs = ps.executeQuery();
      return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
    } catch (SQLException e) {
      throw new RuntimeException("Помилка пошуку виконавця за id=" + id, e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  @Override
  public List<Artist> findAll() {
    String sql = "SELECT * FROM artists ORDER BY name";
    Connection connection = connectionPool.getConnection();
    List<Artist> artists = new ArrayList<>();
    try (Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql)) {
      while (rs.next()) artists.add(mapRow(rs));
      return artists;
    } catch (SQLException e) {
      throw new RuntimeException("Помилка отримання списку виконавців", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  @Override
  public Artist save(Artist artist) {
    return artist.getId() == null ? insert(artist) : update(artist);
  }

  private Artist insert(Artist artist) {
    String sql = "INSERT INTO artists (name, country) VALUES (?, ?)";
    Connection connection = connectionPool.getConnection();
    try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, artist.getName());
      ps.setString(2, artist.getCountry());
      ps.executeUpdate();
      ResultSet keys = ps.getGeneratedKeys();
      if (keys.next()) artist.setId(keys.getLong(1));
      return artist;
    } catch (SQLException e) {
      throw new RuntimeException("Помилка збереження виконавця", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  private Artist update(Artist artist) {
    String sql = "UPDATE artists SET name = ?, country = ? WHERE artist_id = ?";
    Connection connection = connectionPool.getConnection();
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1, artist.getName());
      ps.setString(2, artist.getCountry());
      ps.setLong(3, artist.getId());
      ps.executeUpdate();
      return artist;
    } catch (SQLException e) {
      throw new RuntimeException("Помилка оновлення виконавця", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  @Override
  public void delete(Long id) {
    String sql = "DELETE FROM artists WHERE artist_id = ?";
    Connection connection = connectionPool.getConnection();
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setLong(1, id);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Помилка видалення виконавця id=" + id, e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  @Override
  public List<Artist> findByNameContaining(String query) {
    String sql = "SELECT * FROM artists WHERE name LIKE ? ORDER BY name";
    Connection connection = connectionPool.getConnection();
    List<Artist> artists = new ArrayList<>();
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1, "%" + query + "%");
      ResultSet rs = ps.executeQuery();
      while (rs.next()) artists.add(mapRow(rs));
      return artists;
    } catch (SQLException e) {
      throw new RuntimeException("Помилка пошуку виконавців за назвою", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }
}