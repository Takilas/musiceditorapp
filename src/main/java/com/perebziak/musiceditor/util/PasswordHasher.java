package com.perebziak.musiceditor.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHasher {

  private static final int SALT_ROUNDS = 12;

  public String hash(String plainPassword) {
    return BCrypt.hashpw(plainPassword, BCrypt.gensalt(SALT_ROUNDS));
  }

  public boolean matches(String plainPassword, String hashedPassword) {
    return BCrypt.checkpw(plainPassword, hashedPassword);
  }
}