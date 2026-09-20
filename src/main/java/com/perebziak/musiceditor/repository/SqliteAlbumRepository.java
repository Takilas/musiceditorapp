package com.perebziak.musiceditor.repository;

import com.perebziak.musiceditor.db.ConnectionPool;
import com.perebziak.musiceditor.model.Album;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteAlbumRepository implements AlbumRepository {

  private final ConnectionPool connectionPool;

  public SqliteAlbumRepository(ConnectionPool connectionPool) {
    this.connectionPool = connectionPool;
  }

  private Album mapRow(ResultSet rs) throws SQLException {
    Album album = new Album();
    album.setId(rs.getLong("album_id"));
    album.setTitle(rs.getString("title"));

    int year = rs.getInt("release_year");
    album.setReleaseYear(rs.wasNull() ? null : year);

    album.setArtistId(rs.getLong("artist_id"));
    return album;
  }

  @Override
  public Optional<Album> findById(Long id) {
    String sql = "SELECT * FROM albums WHERE album_id = ?";
    Connection connection = connectionPool.getConnection();
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setLong(1, id);
      ResultSet rs = ps.executeQuery();
      return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
    } catch (SQLException e) {
      throw new RuntimeException("Помилка пошуку альбому за id=" + id, e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  @Override
  public List<Album> findAll() {
    String sql = "SELECT * FROM albums ORDER BY title";
    Connection connection = connectionPool.getConnection();
    List<Album> albums = new ArrayList<>();
    try (Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql)) {
      while (rs.next()) albums.add(mapRow(rs));
      return albums;
    } catch (SQLException e) {
      throw new RuntimeException("Помилка отримання списку альбомів", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  @Override
  public Album save(Album album) {
    return album.getId() == null ? insert(album) : update(album);
  }

  private Album insert(Album album) {
    String sql = "INSERT INTO albums (title, release_year, artist_id) VALUES (?, ?, ?)";
    Connection connection = connectionPool.getConnection();
    try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, album.getTitle());
      if (album.getReleaseYear() != null) ps.setInt(2, album.getReleaseYear());
      else ps.setNull(2, Types.INTEGER);
      ps.setLong(3, album.getArtistId());
      ps.executeUpdate();
      ResultSet keys = ps.getGeneratedKeys();
      if (keys.next()) album.setId(keys.getLong(1));
      return album;
    } catch (SQLException e) {
      throw new RuntimeException("Помилка збереження альбому", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  private Album update(Album album) {
    String sql = "UPDATE albums SET title = ?, release_year = ?, artist_id = ? WHERE album_id = ?";
    Connection connection = connectionPool.getConnection();
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1, album.getTitle());
      if (album.getReleaseYear() != null) ps.setInt(2, album.getReleaseYear());
      else ps.setNull(2, Types.INTEGER);
      ps.setLong(3, album.getArtistId());
      ps.setLong(4, album.getId());
      ps.executeUpdate();
      return album;
    } catch (SQLException e) {
      throw new RuntimeException("Помилка оновлення альбому", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  @Override
  public void delete(Long id) {
    String sql = "DELETE FROM albums WHERE album_id = ?";
    Connection connection = connectionPool.getConnection();
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setLong(1, id);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Помилка видалення альбому id=" + id, e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  @Override
  public List<Album> findByArtistId(Long artistId) {
    String sql = "SELECT * FROM albums WHERE artist_id = ? ORDER BY release_year";
    Connection connection = connectionPool.getConnection();
    List<Album> albums = new ArrayList<>();
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setLong(1, artistId);
      ResultSet rs = ps.executeQuery();
      while (rs.next()) albums.add(mapRow(rs));
      return albums;
    } catch (SQLException e) {
      throw new RuntimeException("Помилка пошуку альбомів виконавця", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  @Override
  public List<Album> findByTitleContaining(String query) {
    String sql = "SELECT * FROM albums WHERE title LIKE ? ORDER BY title";
    Connection connection = connectionPool.getConnection();
    List<Album> albums = new ArrayList<>();
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1, "%" + query + "%");
      ResultSet rs = ps.executeQuery();
      while (rs.next()) albums.add(mapRow(rs));
      return albums;
    } catch (SQLException e) {
      throw new RuntimeException("Помилка пошуку альбомів за назвою", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }
}