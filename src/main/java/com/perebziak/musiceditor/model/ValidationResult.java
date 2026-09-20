package com.perebziak.musiceditor.model;

import lombok.Getter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class ValidationResult {
  private final boolean valid;
  private final List<String> errors;

  private ValidationResult(boolean valid, List<String> errors) {
    this.valid = valid;
    this.errors = errors;
  }

  public static ValidationResult valid() {
    return new ValidationResult(true, Collections.emptyList());
  }

  public static ValidationResult invalid(List<String> errors) {
    return new ValidationResult(false, new ArrayList<>(errors));
  }
}