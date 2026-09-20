package com.perebziak.musiceditor.repository;

import com.perebziak.musiceditor.model.Genre;
import com.perebziak.musiceditor.model.Track;

import java.util.List;

public interface TrackRepository extends Repository<Track, Long> {
  List<Track> findByAlbumId(Long albumId);
  List<Track> findByTitleContaining(String query);
  List<Track> findByGenreId(Long genreId);

  // Робота з self-reference (версії редагування)
  List<Track> findEditedVersions(Long sourceTrackId);
  List<Track> findOriginals();

  // M:N з genres (через track_genres)
  List<Genre> getGenres(Long trackId);
  void addGenre(Long trackId, Long genreId);
  void removeGenre(Long trackId, Long genreId);

  // M:N з users (через favorites)
  List<Track> findFavorites(Long userId);
  void addToFavorites(Long userId, Long trackId);
  void removeFromFavorites(Long userId, Long trackId);
  boolean isFavorite(Long userId, Long trackId);
}