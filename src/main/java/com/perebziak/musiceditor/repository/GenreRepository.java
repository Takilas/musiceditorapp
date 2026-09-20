package com.perebziak.musiceditor.repository;

import com.perebziak.musiceditor.model.Genre;

import java.util.List;
import java.util.Optional;

public interface GenreRepository extends Repository<Genre, Long> {
  Optional<Genre> findByName(String name);
  boolean existsByName(String name);
}