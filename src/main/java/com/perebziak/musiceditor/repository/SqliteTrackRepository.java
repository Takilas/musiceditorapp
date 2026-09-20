package com.perebziak.musiceditor.repository;

import com.perebziak.musiceditor.db.ConnectionPool;
import com.perebziak.musiceditor.model.AudioOperation;
import com.perebziak.musiceditor.model.Genre;
import com.perebziak.musiceditor.model.Track;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteTrackRepository implements TrackRepository {

  private static final DateTimeFormatter SQLITE_DATETIME =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private final ConnectionPool connectionPool;
  private final GenreRepository genreRepository; // композиція

  public SqliteTrackRepository(ConnectionPool connectionPool, GenreRepository genreRepository) {
    this.connectionPool = connectionPool;
    this.genreRepository = genreRepository;
  }

  private Track mapRow(ResultSet rs) throws SQLException {
    Track track = new Track();
    track.setId(rs.getLong("track_id"));
    track.setTitle(rs.getString("title"));
    track.setDurationSeconds(rs.getInt("duration_seconds"));
    track.setFilePath(rs.getString("file_path"));

    long albumId = rs.getLong("album_id");
    track.setAlbumId(rs.wasNull() ? null : albumId);

    String addedDate = rs.getString("added_date");
    if (addedDate != null) track.setAddedDate(LocalDateTime.parse(addedDate.replace(" ", "T")));

    long sourceId = rs.getLong("source_track_id");
    track.setSourceTrackId(rs.wasNull() ? null : sourceId);

    String editType = rs.getString("edit_type");
    if (editType != null) track.setEditType(AudioOperation.valueOf(editType));

    track.setEditParams(rs.getString("edit_params"));

    long editedBy = rs.getLong("edited_by");
    track.setEditedBy(rs.wasNull() ? null : editedBy);

    String editedDate = rs.getString("edited_date");
    if (editedDate != null) track.setEditedDate(LocalDateTime.parse(editedDate.replace(" ", "T")));

    return track;
  }

  private void setNullableLong(PreparedStatement ps, int index, Long value) throws SQLException {
    if (value != null) ps.setLong(index, value);
    else ps.setNull(index, Types.BIGINT);
  }

  @Override
  public Optional<Track> findById(Long id) {
    String sql = "SELECT * FROM tracks WHERE track_id = ?";
    Connection connection = connectionPool.getConnection();
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setLong(1, id);
      ResultSet rs = ps.executeQuery();
      return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
    } catch (SQLException e) {
      throw new RuntimeException("Помилка пошуку треку за id=" + id, e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  @Override
  public List<Track> findAll() {
    String sql = "SELECT * FROM tracks ORDER BY title";
    Connection connection = connectionPool.getConnection();
    List<Track> tracks = new ArrayList<>();
    try (Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql)) {
      while (rs.next()) tracks.add(mapRow(rs));
      return tracks;
    } catch (SQLException e) {
      throw new RuntimeException("Помилка отримання списку треків", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  @Override
  public Track save(Track track) {
    return track.getId() == null ? insert(track) : update(track);
  }

  private Track insert(Track track) {
    String sql = """
        INSERT INTO tracks (title, duration_seconds, file_path, album_id,
                             source_track_id, edit_type, edit_params, edited_by, edited_date)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
    Connection connection = connectionPool.getConnection();
    try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, track.getTitle());
      ps.setInt(2, track.getDurationSeconds());
      ps.setString(3, track.getFilePath());
      setNullableLong(ps, 4, track.getAlbumId());
      setNullableLong(ps, 5, track.getSourceTrackId());
      ps.setString(6, track.getEditType() != null ? track.getEditType().name() : null);
      ps.setString(7, track.getEditParams());
      setNullableLong(ps, 8, track.getEditedBy());
      ps.setString(9, track.getEditedDate() != null ? track.getEditedDate().format(SQLITE_DATETIME) : null);
      ps.executeUpdate();
      ResultSet keys = ps.getGeneratedKeys();
      if (keys.next()) track.setId(keys.getLong(1));
      return track;
    } catch (SQLException e) {
      throw new RuntimeException("Помилка збереження треку", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  // Оновлюємо тільки "базові" поля метаданих; поля версіонування
  // (edit_type/source_track_id/edited_by/edited_date) виставляються
  // один раз при створенні відредагованої версії і далі не змінюються.
  private Track update(Track track) {
    String sql = "UPDATE tracks SET title = ?, duration_seconds = ?, file_path = ?, album_id = ? WHERE track_id = ?";
    Connection connection = connectionPool.getConnection();
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1, track.getTitle());
      ps.setInt(2, track.getDurationSeconds());
      ps.setString(3, track.getFilePath());
      setNullableLong(ps, 4, track.getAlbumId());
      ps.setLong(5, track.getId());
      ps.executeUpdate();
      return track;
    } catch (SQLException e) {
      throw new RuntimeException("Помилка оновлення треку", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  @Override
  public void delete(Long id) {
    String sql = "DELETE FROM tracks WHERE track_id = ?";
    Connection connection = connectionPool.getConnection();
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setLong(1, id);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Помилка видалення треку id=" + id, e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  @Override
  public List<Track> findByAlbumId(Long albumId) {
    return queryList("SELECT * FROM tracks WHERE album_id = ? ORDER BY title", albumId);
  }

  @Override
  public List<Track> findEditedVersions(Long sourceTrackId) {
    return queryList("SELECT * FROM tracks WHERE source_track_id = ? ORDER BY edited_date", sourceTrackId);
  }

  @Override
  public List<Track> findOriginals() {
    String sql = "SELECT * FROM tracks WHERE source_track_id IS NULL ORDER BY title";
    Connection connection = connectionPool.getConnection();
    List<Track> tracks = new ArrayList<>();
    try (Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql)) {
      while (rs.next()) tracks.add(mapRow(rs));
      return tracks;
    } catch (SQLException e) {
      throw new RuntimeException("Помилка пошуку оригінальних треків", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  private List<Track> queryList(String sql, Long param) {
    Connection connection = connectionPool.getConnection();
    List<Track> tracks = new ArrayList<>();
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setLong(1, param);
      ResultSet rs = ps.executeQuery();
      while (rs.next()) tracks.add(mapRow(rs));
      return tracks;
    } catch (SQLException e) {
      throw new RuntimeException("Помилка виконання запиту треків", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  @Override
  public List<Track> findByTitleContaining(String query) {
    String sql = "SELECT * FROM tracks WHERE title LIKE ? ORDER BY title";
    Connection connection = connectionPool.getConnection();
    List<Track> tracks = new ArrayList<>();
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1, "%" + query + "%");
      ResultSet rs = ps.executeQuery();
      while (rs.next()) tracks.add(mapRow(rs));
      return tracks;
    } catch (SQLException e) {
      throw new RuntimeException("Помилка пошуку треків за назвою", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  @Override
  public List<Track> findByGenreId(Long genreId) {
    String sql = """
        SELECT t.* FROM tracks t
        JOIN track_genres tg ON tg.track_id = t.track_id
        WHERE tg.genre_id = ?
        ORDER BY t.title
        """;
    Connection connection = connectionPool.getConnection();
    List<Track> tracks = new ArrayList<>();
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setLong(1, genreId);
      ResultSet rs = ps.executeQuery();
      while (rs.next()) tracks.add(mapRow(rs));
      return tracks;
    } catch (SQLException e) {
      throw new RuntimeException("Помилка пошуку треків за жанром", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  @Override
  public List<Genre> getGenres(Long trackId) {
    String sql = "SELECT genre_id FROM track_genres WHERE track_id = ?";
    Connection connection = connectionPool.getConnection();
    List<Genre> genres = new ArrayList<>();
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setLong(1, trackId);
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        genreRepository.findById(rs.getLong("genre_id")).ifPresent(genres::add);
      }
      return genres;
    } catch (SQLException e) {
      throw new RuntimeException("Помилка отримання жанрів треку", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  @Override
  public void addGenre(Long trackId, Long genreId) {
    String sql = "INSERT INTO track_genres (track_id, genre_id) VALUES (?, ?)";
    Connection connection = connectionPool.getConnection();
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setLong(1, trackId);
      ps.setLong(2, genreId);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Помилка додавання жанру до треку", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  @Override
  public void removeGenre(Long trackId, Long genreId) {
    String sql = "DELETE FROM track_genres WHERE track_id = ? AND genre_id = ?";
    Connection connection = connectionPool.getConnection();
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setLong(1, trackId);
      ps.setLong(2, genreId);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Помилка видалення жанру з треку", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  @Override
  public List<Track> findFavorites(Long userId) {
    String sql = """
        SELECT t.* FROM tracks t
        JOIN favorites f ON f.track_id = t.track_id
        WHERE f.user_id = ?
        ORDER BY f.added_date DESC
        """;
    Connection connection = connectionPool.getConnection();
    List<Track> tracks = new ArrayList<>();
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setLong(1, userId);
      ResultSet rs = ps.executeQuery();
      while (rs.next()) tracks.add(mapRow(rs));
      return tracks;
    } catch (SQLException e) {
      throw new RuntimeException("Помилка отримання улюблених треків", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  @Override
  public void addToFavorites(Long userId, Long trackId) {
    String sql = "INSERT INTO favorites (user_id, track_id) VALUES (?, ?)";
    Connection connection = connectionPool.getConnection();
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setLong(1, userId);
      ps.setLong(2, trackId);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Помилка додавання треку в улюблене", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  @Override
  public void removeFromFavorites(Long userId, Long trackId) {
    String sql = "DELETE FROM favorites WHERE user_id = ? AND track_id = ?";
    Connection connection = connectionPool.getConnection();
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setLong(1, userId);
      ps.setLong(2, trackId);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Помилка видалення треку з улюбленого", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }

  @Override
  public boolean isFavorite(Long userId, Long trackId) {
    String sql = "SELECT 1 FROM favorites WHERE user_id = ? AND track_id = ?";
    Connection connection = connectionPool.getConnection();
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setLong(1, userId);
      ps.setLong(2, trackId);
      return ps.executeQuery().next();
    } catch (SQLException e) {
      throw new RuntimeException("Помилка перевірки улюбленого треку", e);
    } finally {
      connectionPool.releaseConnection(connection);
    }
  }
}