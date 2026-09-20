package com.perebziak.musiceditor.repository;

import com.perebziak.musiceditor.db.ConnectionPool;
import com.perebziak.musiceditor.model.Playlist;
import com.perebziak.musiceditor.model.Track;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqlitePlaylistRepository implements PlaylistRepository {

  private final ConnectionPool connectionPool;
  private final TrackRepository trackRepository; // композиція

  public SqlitePlaylistRepository(ConnectionPool connectionPool, TrackRepository trackRepository) {
    this.connectionPool = connectionPool;
    this.trackRepository = trackRepository;
  }

  private Playlist mapRow(ResultSet rs) throws SQLException {
    Playlist playlist = new Playlist();
    playlist.setId(rs.getLong("playlist_id"));
    playlist.setName(rs.getString("name"));
    playlist.setUserId(rs.getLong("user_id"));
    String createdDate = rs.getString("created_date");
    if (createdDate != null) playlist.setCreatedDate(LocalDateTime.parse(createdDate.replace(" ", "T")));
    return playlist;
  }

  @Override
  public Optional<Playlist> findById(Long id) {
    String sql = "SELECT * FROM playlists WHERE playlist_id = ?";
    Connection connection = connectionPool.getConnection();
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setLong(1, id);
      ResultSet rs = ps.executeQuery();
      return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
    } catch (SQLException e) {
      throw new RuntimeException("Помилка пошуку плейлиста за id=" + id, e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  @Override
  public List<Playlist> findAll() {
    String sql = "SELECT * FROM playlists ORDER BY created_date DESC";
    Connection connection = connectionPool.getConnection();
    List<Playlist> playlists = new ArrayList<>();
    try (Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql)) {
      while (rs.next()) playlists.add(mapRow(rs));
      return playlists;
    } catch (SQLException e) {
      throw new RuntimeException("Помилка отримання списку плейлистів", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  @Override
  public Playlist save(Playlist playlist) {
    return playlist.getId() == null ? insert(playlist) : update(playlist);
  }

  private Playlist insert(Playlist playlist) {
    String sql = "INSERT INTO playlists (name, user_id) VALUES (?, ?)";
    Connection connection = connectionPool.getConnection();
    try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, playlist.getName());
      ps.setLong(2, playlist.getUserId());
      ps.executeUpdate();
      ResultSet keys = ps.getGeneratedKeys();
      if (keys.next()) playlist.setId(keys.getLong(1));
      return playlist;
    } catch (SQLException e) {
      throw new RuntimeException("Помилка збереження плейлиста", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  private Playlist update(Playlist playlist) {
    String sql = "UPDATE playlists SET name = ? WHERE playlist_id = ?";
    Connection connection = connectionPool.getConnection();
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1, playlist.getName());
      ps.setLong(2, playlist.getId());
      ps.executeUpdate();
      return playlist;
    } catch (SQLException e) {
      throw new RuntimeException("Помилка оновлення плейлиста", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  @Override
  public void delete(Long id) {
    String sql = "DELETE FROM playlists WHERE playlist_id = ?";
    Connection connection = connectionPool.getConnection();
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setLong(1, id);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Помилка видалення плейлиста id=" + id, e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  @Override
  public List<Playlist> findByUserId(Long userId) {
    String sql = "SELECT * FROM playlists WHERE user_id = ? ORDER BY created_date DESC";
    Connection connection = connectionPool.getConnection();
    List<Playlist> playlists = new ArrayList<>();
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setLong(1, userId);
      ResultSet rs = ps.executeQuery();
      while (rs.next()) playlists.add(mapRow(rs));
      return playlists;
    } catch (SQLException e) {
      throw new RuntimeException("Помилка пошуку плейлистів користувача", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  @Override
  public List<Track> getTracks(Long playlistId) {
    String sql = "SELECT track_id FROM playlist_tracks WHERE playlist_id = ? ORDER BY position";
    Connection connection = connectionPool.getConnection();
    List<Track> tracks = new ArrayList<>();
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setLong(1, playlistId);
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        trackRepository.findById(rs.getLong("track_id")).ifPresent(tracks::add);
      }
      return tracks;
    } catch (SQLException e) {
      throw new RuntimeException("Помилка отримання треків плейлиста", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  @Override
  public void addTrack(Long playlistId, Long trackId, int position) {
    String sql = "INSERT INTO playlist_tracks (playlist_id, track_id, position) VALUES (?, ?, ?)";
    Connection connection = connectionPool.getConnection();
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setLong(1, playlistId);
      ps.setLong(2, trackId);
      ps.setInt(3, position);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Помилка додавання треку в плейлист", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  @Override
  public void removeTrack(Long playlistId, Long trackId) {
    String sql = "DELETE FROM playlist_tracks WHERE playlist_id = ? AND track_id = ?";
    Connection connection = connectionPool.getConnection();
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setLong(1, playlistId);
      ps.setLong(2, trackId);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Помилка видалення треку з плейлиста", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }
}