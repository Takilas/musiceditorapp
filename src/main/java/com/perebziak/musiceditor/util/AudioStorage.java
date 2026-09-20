package com.perebziak.musiceditor.util;

import com.perebziak.musiceditor.exception.AudioProcessingException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class AudioStorage {

  private static final String STORAGE_DIR = "audio_storage";

  public String store(File sourceFile) {
    try {
      Path storageDir = Paths.get(STORAGE_DIR);
      if (!Files.exists(storageDir)) {
        Files.createDirectories(storageDir);
      }

      String extension = getExtension(sourceFile.getName());
      String storedFileName = UUID.randomUUID() + "." + extension;
      Path targetPath = storageDir.resolve(storedFileName);

      Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
      return targetPath.toString();
    } catch (IOException e) {
      throw new AudioProcessingException("Не вдалося зберегти аудіофайл: " + sourceFile.getName(), e);
    }
  }

  public String getExtension(String fileName) {
    int dotIndex = fileName.lastIndexOf('.');
    if (dotIndex == -1 || dotIndex == fileName.length() - 1) {
      throw new AudioProcessingException("Файл не має розширення: " + fileName);
    }
    return fileName.substring(dotIndex + 1).toLowerCase();
  }
}