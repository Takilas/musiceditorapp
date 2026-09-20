package com.perebziak.musiceditor.repository;

import com.perebziak.musiceditor.model.Album;

import java.util.List;

public interface AlbumRepository extends Repository<Album, Long> {
  List<Album> findByArtistId(Long artistId);
  List<Album> findByTitleContaining(String query);
}