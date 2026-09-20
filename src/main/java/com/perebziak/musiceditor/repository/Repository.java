package com.perebziak.musiceditor.repository;

import java.util.List;
import java.util.Optional;

public interface Repository<T, ID> {
  Optional<T> findById(ID id);
  List<T> findAll();
  T save(T entity);       // INSERT якщо id==null, UPDATE якщо id вже є
  void delete(ID id);
}