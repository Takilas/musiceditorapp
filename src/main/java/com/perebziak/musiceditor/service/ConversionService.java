package com.perebziak.musiceditor.service;

import com.perebziak.musiceditor.exception.PaymentException;
import com.perebziak.musiceditor.model.ConversionOrder;
import com.perebziak.musiceditor.model.Track;
import com.perebziak.musiceditor.repository.ConversionOrderRepository;
import com.perebziak.musiceditor.repository.TrackRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public class ConversionService {

  private static final Map<String, BigDecimal> PRICES = Map.of(
      "WAV", new BigDecimal("15.00"),
      "MP3", new BigDecimal("20.00"),
      "MP4", new BigDecimal("25.00")
  );

  private final ConversionOrderRepository orderRepository;
  private final TrackRepository trackRepository;
  private final FfmpegService ffmpegService;
  private final PaymentService paymentService;

  public ConversionService(ConversionOrderRepository orderRepository,
      TrackRepository trackRepository,
      FfmpegService ffmpegService,
      PaymentService paymentService) {
    this.orderRepository = orderRepository;
    this.trackRepository = trackRepository;
    this.ffmpegService = ffmpegService;
    this.paymentService = paymentService;
  }

  public BigDecimal getPrice(String targetFormat) {
    BigDecimal price = PRICES.get(targetFormat.toUpperCase());
    if (price == null) {
      throw new IllegalArgumentException("Непідтримуваний формат: " + targetFormat);
    }
    return price;
  }

  /**
   * Повний бізнес-сценарій: створення замовлення -> оплата -> конвертація.
   * При невдалій оплаті замовлення лишається зі статусом FAILED (аудит-слід,
   * навіть неуспішні спроби залишаються в історії — типова банківська практика).
   */
  public ConversionOrder convertTrack(Long userId, Track track, String targetFormat,
      String cardNumber, String expiry, String cvv) {
    BigDecimal price = getPrice(targetFormat);

    ConversionOrder order = new ConversionOrder();
    order.setUserId(userId);
    order.setTrackId(track.getId());
    order.setTargetFormat(targetFormat.toUpperCase());
    order.setPrice(price);
    order.setStatus("PENDING");
    order = orderRepository.save(order);

    try {
      paymentService.processPayment(cardNumber, expiry, cvv, price);
    } catch (PaymentException e) {
      order.setStatus("FAILED");
      orderRepository.save(order);
      throw e;
    }

    order.setStatus("PAID");
    orderRepository.save(order);

    String resultPath = ffmpegService.convert(track.getFilePath(), targetFormat);

    order.setStatus("COMPLETED");
    order.setResultFilePath(resultPath);
    order.setCompletedDate(LocalDateTime.now());
    return orderRepository.save(order);
  }
}