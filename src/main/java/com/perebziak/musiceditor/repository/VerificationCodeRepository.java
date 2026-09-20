package com.perebziak.musiceditor.repository;

import com.perebziak.musiceditor.model.VerificationCode;
import java.util.Optional;

public interface VerificationCodeRepository extends Repository<VerificationCode, Long> {
  Optional<VerificationCode> findActiveByEmail(String email);
  void markAsUsed(Long id);
  void deleteExpired();
}