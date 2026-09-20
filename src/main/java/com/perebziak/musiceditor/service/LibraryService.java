package com.perebziak.musiceditor.service;

import com.perebziak.musiceditor.exception.AudioProcessingException;
import com.perebziak.musiceditor.exception.ValidationException;
import com.perebziak.musiceditor.model.Track;
import com.perebziak.musiceditor.repository.TrackRepository;
import com.perebziak.musiceditor.util.AudioStorage;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class LibraryService {

  private static final Set<String> SUPPORTED_FORMATS = Set.of("mp3", "wav");

  private final TrackRepository trackRepository;
  private final AudioStorage audioStorage;

  public LibraryService(TrackRepository trackRepository, AudioStorage audioStorage) {
    this.trackRepository = trackRepository;
    this.audioStorage = audioStorage;
  }

  public List<Track> getAllTracks() {
    return trackRepository.findAll();
  }

  public List<Track> searchTracks(String query) {
    if (query == null || query.isBlank()) {
      return getAllTracks();
    }
    return trackRepository.findByTitleContaining(query.trim());
  }

  public Optional<Track> getTrackById(Long id) {
    return trackRepository.findById(id);
  }

  public List<Track> getEditedVersions(Long sourceTrackId) {
    return trackRepository.findEditedVersions(sourceTrackId);
  }

  /**
   * Додає новий трек у бібліотеку: копіює файл у власне сховище (audio_storage/),
   * рахує тривалість (для WAV — точно, для MP3 — тимчасова заглушка,
   * буде виправлено після підключення mp3-бібліотеки на етапі обробки звуку)
   * і зберігає запис у БД.
   */
  public Track addTrack(File sourceFile, String title) {
    if (title == null || title.isBlank()) {
      throw new ValidationException("Назва треку не може бути порожньою");
    }
    if (sourceFile == null || !sourceFile.exists()) {
      throw new ValidationException("Обраний файл не знайдено");
    }

    String extension = audioStorage.getExtension(sourceFile.getName());
    if (!SUPPORTED_FORMATS.contains(extension)) {
      throw new ValidationException("Підтримуються лише формати MP3 та WAV");
    }

    String storedPath = audioStorage.store(sourceFile);

    Track track = new Track();
    track.setTitle(title.trim());
    track.setDurationSeconds(0); // оновиться асинхронно після зчитування метаданих
    track.setFilePath(storedPath);
    track.setAlbumId(null);
    track.setAddedDate(LocalDateTime.now());

    return trackRepository.save(track);
  }

  public void updateDuration(Long trackId, int durationSeconds) {
    trackRepository.findById(trackId).ifPresent(track -> {
      track.setDurationSeconds(durationSeconds);
      trackRepository.save(track);
    });
  }

  public void deleteTrack(Long trackId) {
    trackRepository.delete(trackId);
  }

  private int calculateDuration(File file, String extension) {
    if (!"wav".equals(extension)) {
      // MP3: точний розрахунок тривалості потребує окремої бібліотеки
      // (mp3spi/JLayer)
      return 0;
    }
    try (AudioInputStream stream = AudioSystem.getAudioInputStream(file)) {
      AudioFormat format = stream.getFormat();
      long frames = stream.getFrameLength();
      double seconds = frames / format.getFrameRate();
      return (int) Math.round(seconds);
    } catch (UnsupportedAudioFileException | IOException e) {
      throw new AudioProcessingException("Не вдалося прочитати аудіофайл: " + file.getName(), e);
    }
  }
}