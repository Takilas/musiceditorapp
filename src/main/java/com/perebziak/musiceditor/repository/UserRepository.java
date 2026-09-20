package com.perebziak.musiceditor.repository;

import com.perebziak.musiceditor.model.User;
import java.util.Optional;

public interface UserRepository extends Repository<User, Long> {
  Optional<User> findByEmail(String email);
  Optional<User> findByUsername(String username);
  boolean existsByEmail(String email);
  boolean existsByUsername(String username);
}