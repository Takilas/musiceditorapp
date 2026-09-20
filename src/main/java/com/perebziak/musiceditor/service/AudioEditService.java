package com.perebziak.musiceditor.service;

import com.perebziak.musiceditor.exception.ValidationException;
import com.perebziak.musiceditor.model.AudioOperation;
import com.perebziak.musiceditor.model.Track;
import com.perebziak.musiceditor.repository.TrackRepository;

import java.time.LocalDateTime;

public class AudioEditService {

  private final TrackRepository trackRepository;
  private final FfmpegService ffmpegService;

  public AudioEditService(TrackRepository trackRepository, FfmpegService ffmpegService) {
    this.trackRepository = trackRepository;
    this.ffmpegService = ffmpegService;
  }

  public Track cut(Track source, int startSeconds, int endSeconds, Long editedByUserId) {
    if (startSeconds < 0 || endSeconds <= startSeconds) {
      throw new ValidationException("Некоректний діапазон обрізки: кінець має бути більшим за початок");
    }
    String outputPath = ffmpegService.trim(source.getFilePath(), startSeconds, endSeconds);
    String params = "start=" + formatTime(startSeconds) + ",end=" + formatTime(endSeconds);
    return saveEditedVersion(source, AudioOperation.TRIM, params, outputPath, editedByUserId,
        endSeconds - startSeconds);
  }

  public Track changeVolume(Track source, double gain, Long editedByUserId) {
    if (gain <= 0 || gain > 5) {
      throw new ValidationException("Гучність має бути в межах від 0.1 до 5.0");
    }
    String outputPath = ffmpegService.changeVolume(source.getFilePath(), gain);
    return saveEditedVersion(source, AudioOperation.VOLUME, "gain=" + gain, outputPath,
        editedByUserId, source.getDurationSeconds());
  }

  public Track changeSpeed(Track source, double speedFactor, Long editedByUserId) {
    if (speedFactor < 0.5 || speedFactor > 2.0) {
      throw new ValidationException("Швидкість має бути в межах від 0.5 до 2.0");
    }
    String outputPath = ffmpegService.changeSpeed(source.getFilePath(), speedFactor);
    int newDuration = source.getDurationSeconds() > 0
        ? (int) Math.round(source.getDurationSeconds() / speedFactor) : 0;
    return saveEditedVersion(source, AudioOperation.SPEED, "speed=" + speedFactor, outputPath,
        editedByUserId, newDuration);
  }

  public Track changePitch(Track source, double pitchFactor, Long editedByUserId) {
    if (pitchFactor < 0.5 || pitchFactor > 2.0) {
      throw new ValidationException("Висота тону має бути в межах від 0.5 до 2.0");
    }
    String outputPath = ffmpegService.changePitch(source.getFilePath(), pitchFactor);
    return saveEditedVersion(source, AudioOperation.PITCH, "pitch=" + pitchFactor, outputPath,
        editedByUserId, 0);
  }

  private Track saveEditedVersion(Track source, AudioOperation operation, String params,
      String outputPath, Long editedByUserId, int durationSeconds) {
    Track version = new Track();
    version.setTitle(source.getTitle() + " (" + operationLabel(operation) + ")");
    version.setDurationSeconds(durationSeconds);
    version.setFilePath(outputPath);
    version.setAlbumId(source.getAlbumId());
    version.setAddedDate(LocalDateTime.now());
    version.setSourceTrackId(source.getId());
    version.setEditType(operation);
    version.setEditParams(params);
    version.setEditedBy(editedByUserId);
    version.setEditedDate(LocalDateTime.now());
    return trackRepository.save(version);
  }

  private String operationLabel(AudioOperation operation) {
    return switch (operation) {
      case TRIM -> "обрізано";
      case VOLUME -> "гучність";
      case SPEED -> "швидкість";
      case PITCH -> "тон";
    };
  }

  private String formatTime(int totalSeconds) {
    return String.format("00:%02d:%02d", totalSeconds / 60, totalSeconds % 60);
  }
}