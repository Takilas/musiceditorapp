package com.perebziak.musiceditor.repository;

import com.perebziak.musiceditor.model.Playlist;
import com.perebziak.musiceditor.model.Track;

import java.util.List;

public interface PlaylistRepository extends Repository<Playlist, Long> {
  List<Playlist> findByUserId(Long userId);
  List<Track> getTracks(Long playlistId);
  void addTrack(Long playlistId, Long trackId, int position);
  void removeTrack(Long playlistId, Long trackId);
}