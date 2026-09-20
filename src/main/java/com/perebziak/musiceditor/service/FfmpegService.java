package com.perebziak.musiceditor.service;

import com.perebziak.musiceditor.exception.AudioProcessingException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class FfmpegService {

  private static final String CONVERTED_DIR = "audio_storage/converted";
  private static final String EDITED_DIR = "audio_storage/edited";

  public String convert(String sourceFilePath, String targetFormat) {
    Path output = buildOutputPath(CONVERTED_DIR, targetFormat.toLowerCase());
    runFfmpeg(List.of("-y", "-i", sourceFilePath, output.toString()));
    return output.toString();
  }

  public String trim(String sourceFilePath, int startSeconds, int endSeconds) {
    Path output = buildOutputPath(EDITED_DIR, getExtension(sourceFilePath));
    runFfmpeg(List.of(
        "-y", "-i", sourceFilePath,
        "-ss", String.valueOf(startSeconds),
        "-to", String.valueOf(endSeconds),
        output.toString()
    ));
    return output.toString();
  }

  public String changeVolume(String sourceFilePath, double gain) {
    Path output = buildOutputPath(EDITED_DIR, getExtension(sourceFilePath));
    String filter = String.format(Locale.US, "volume=%.2f", gain);
    runFfmpeg(List.of("-y", "-i", sourceFilePath, "-filter:a", filter, output.toString()));
    return output.toString();
  }

  /**
   * Зміна швидкості через фільтр atempo — темп змінюється, висота тону
   * лишається незмінною. Вбудований у FFmpeg, додаткових бібліотек не потребує.
   */
  public String changeSpeed(String sourceFilePath, double speedFactor) {
    Path output = buildOutputPath(EDITED_DIR, getExtension(sourceFilePath));
    String filter = String.format(Locale.US, "atempo=%.2f", speedFactor);
    runFfmpeg(List.of("-y", "-i", sourceFilePath, "-filter:a", filter, output.toString()));
    return output.toString();
  }

  /**
   * Зміна висоти тону через resampling (asetrate+aresample) — простий підхід
   * без додаткових бібліотек. Побічний ефект: тривалість файлу теж трохи
   * змінюється (узгоджене спрощення для етапу практики).
   */
  public String changePitch(String sourceFilePath, double pitchFactor) {
    Path output = buildOutputPath(EDITED_DIR, getExtension(sourceFilePath));
    String filter = String.format(Locale.US, "asetrate=44100*%.2f,aresample=44100", pitchFactor);
    runFfmpeg(List.of("-y", "-i", sourceFilePath, "-filter:a", filter, output.toString()));
    return output.toString();
  }

  private void runFfmpeg(List<String> args) {
    try {
      List<String> command = new ArrayList<>();
      command.add("ffmpeg");
      command.addAll(args);

      ProcessBuilder builder = new ProcessBuilder(command);
      builder.redirectErrorStream(true);
      Process process = builder.start();

      try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
        while (reader.readLine() != null) {
          // лог FFmpeg ігноруємо
        }
      }

      boolean finished = process.waitFor(60, TimeUnit.SECONDS);
      if (!finished) {
        process.destroyForcibly();
        throw new AudioProcessingException("Обробка перевищила ліміт часу (60 сек)");
      }
      if (process.exitValue() != 0) {
        throw new AudioProcessingException("FFmpeg завершився з помилкою (код " + process.exitValue() + ")");
      }
    } catch (IOException e) {
      throw new AudioProcessingException(
          "Не вдалося запустити FFmpeg. Перевірте, що він встановлений і доданий у PATH.", e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new AudioProcessingException("Обробку перервано", e);
    }
  }

  private Path buildOutputPath(String dir, String extension) {
    try {
      Path outputDir = Paths.get(dir);
      if (!Files.exists(outputDir)) {
        Files.createDirectories(outputDir);
      }
      return outputDir.resolve(UUID.randomUUID() + "." + extension);
    } catch (IOException e) {
      throw new AudioProcessingException("Не вдалося створити папку: " + dir, e);
    }
  }

  private String getExtension(String filePath) {
    int dotIndex = filePath.lastIndexOf('.');
    return dotIndex == -1 ? "mp3" : filePath.substring(dotIndex + 1).toLowerCase();
  }
}