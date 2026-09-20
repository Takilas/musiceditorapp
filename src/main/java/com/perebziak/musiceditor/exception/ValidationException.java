package com.perebziak.musiceditor.exception;

import java.util.Collections;
import java.util.List;

public class ValidationException extends AppException {

  private final List<String> errors;

  public ValidationException(List<String> errors) {
    super("Помилка валідації: " + String.join("; ", errors));
    this.errors = errors;
  }

  public ValidationException(String singleError) {
    this(Collections.singletonList(singleError));
  }

  public List<String> getErrors() {
    return errors;
  }
}