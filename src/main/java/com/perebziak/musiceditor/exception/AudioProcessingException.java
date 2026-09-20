package com.perebziak.musiceditor.exception;

public class AudioProcessingException extends AppException {
  public AudioProcessingException(String message) {
    super(message);
  }

  public AudioProcessingException(String message, Throwable cause) {
    super(message, cause);
  }
}