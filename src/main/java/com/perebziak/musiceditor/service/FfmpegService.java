package com.perebziak.musiceditor.service;

import com.perebziak.musiceditor.exception.AudioProcessingException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class FfmpegService {

  private static final String CONVERTED_DIR = "audio_storage/converted";

  /**
   * Конвертує аудіофайл у цільовий формат через зовнішню програму FFmpeg.
   * FFmpeg має бути встановлений в системі і доступний через PATH.
   */
  public String convert(String sourceFilePath, String targetFormat) {
    try {
      Path outputDir = Paths.get(CONVERTED_DIR);
      if (!Files.exists(outputDir)) {
        Files.createDirectories(outputDir);
      }

      String outputFileName = UUID.randomUUID() + "." + targetFormat.toLowerCase();
      Path outputPath = outputDir.resolve(outputFileName);

      ProcessBuilder builder = new ProcessBuilder(
          "ffmpeg",
          "-y",                          // перезаписати, якщо файл існує
          "-i", sourceFilePath,          // вхідний файл
          outputPath.toString()          // вихідний файл (формат визначається по розширенню)
      );
      builder.redirectErrorStream(true);

      Process process = builder.start();

      // FFmpeg пише прогрес в stderr/stdout — вичитуємо, щоб процес не завис
      // через переповнений буфер виводу
      try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
        while (reader.readLine() != null) {
          // логи FFmpeg тут ігноруємо, за потреби можна виводити прогрес
        }
      }

      boolean finished = process.waitFor(60, TimeUnit.SECONDS);
      if (!finished) {
        process.destroyForcibly();
        throw new AudioProcessingException("Конвертація перевищила ліміт часу (60 сек)");
      }

      if (process.exitValue() != 0) {
        throw new AudioProcessingException("FFmpeg завершився з помилкою (код " + process.exitValue() + ")");
      }

      if (!Files.exists(outputPath)) {
        throw new AudioProcessingException("Конвертація не створила вихідний файл");
      }

      return outputPath.toString();
    } catch (IOException e) {
      throw new AudioProcessingException(
          "Не вдалося запустити FFmpeg. Перевірте, що він встановлений і доданий у PATH.", e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new AudioProcessingException("Конвертацію перервано", e);
    }
  }
}