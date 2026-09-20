package com.perebziak.musiceditor.repository;

import com.perebziak.musiceditor.model.Artist;

import java.util.List;

public interface ArtistRepository extends Repository<Artist, Long> {
  List<Artist> findByNameContaining(String query);
}