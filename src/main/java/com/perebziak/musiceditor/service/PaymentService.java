package com.perebziak.musiceditor.service;

import com.perebziak.musiceditor.exception.PaymentException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

public class PaymentService {

  /**
   * Перевірка номера картки алгоритмом Луна (Luhn algorithm) —
   * стандартний контрольний алгоритм, який реально використовують
   * платіжні системи для базової валідації номера картки.
   */
  public boolean isValidCardNumber(String cardNumber) {
    String digitsOnly = cardNumber.replaceAll("\\s+", "");
    if (!digitsOnly.matches("\\d{13,19}")) {
      return false;
    }

    int sum = 0;
    boolean alternate = false;
    for (int i = digitsOnly.length() - 1; i >= 0; i--) {
      int digit = Character.getNumericValue(digitsOnly.charAt(i));
      if (alternate) {
        digit *= 2;
        if (digit > 9) {
          digit -= 9;
        }
      }
      sum += digit;
      alternate = !alternate;
    }
    return sum % 10 == 0;
  }

  public boolean isValidExpiryDate(String expiryMMYY) {
    if (!expiryMMYY.matches("(0[1-9]|1[0-2])/\\d{2}")) {
      return false;
    }
    try {
      String[] parts = expiryMMYY.split("/");
      int month = Integer.parseInt(parts[0]);
      int year = 2000 + Integer.parseInt(parts[1]);
      LocalDate expiry = LocalDate.of(year, month, 1).plusMonths(1).minusDays(1);
      return !expiry.isBefore(LocalDate.now());
    } catch (Exception e) {
      return false;
    }
  }

  public boolean isValidCvv(String cvv) {
    return cvv.matches("\\d{3}");
  }

  /**
   * Імітація обробки оплати. Реальна інтеграція з платіжним шлюзом
   * (Stripe/LiqPay/Fondy) вимагає реєстрації ФОП і мерчант-акаунту —
   * поза межами навчального проєкту. Тут — валідація введених даних
   * карткою за реальними алгоритмами (Луна, строк дії) + симуляція
   * випадкової відмови банку (5% шанс), щоб показати обробку
   * негативного сценарію теж.
   */
  public void processPayment(String cardNumber, String expiryMMYY, String cvv, java.math.BigDecimal amount) {
    if (!isValidCardNumber(cardNumber)) {
      throw new PaymentException("Некоректний номер картки");
    }
    if (!isValidExpiryDate(expiryMMYY)) {
      throw new PaymentException("Некоректний або прострочений термін дії картки");
    }
    if (!isValidCvv(cvv)) {
      throw new PaymentException("Некоректний CVV код");
    }

    // Симуляція звернення до банку (затримка як в реальному запиті)
    try {
      Thread.sleep(800);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    if (ThreadLocalRandom.current().nextInt(100) < 5) {
      throw new PaymentException("Банк відхилив транзакцію. Спробуйте іншу картку.");
    }
  }
}