package com.perebziak.musiceditor.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentServiceTest {

  private final PaymentService paymentService = new PaymentService();

  @ParameterizedTest
  @ValueSource(strings = {"4532015112830366", "4916338506082832", "5425233430109903"})
  void isValidCardNumber_returnsTrue_forValidLuhnNumbers(String cardNumber) {
    assertTrue(paymentService.isValidCardNumber(cardNumber));
  }

  @ParameterizedTest
  @ValueSource(strings = {"1234567890123456", "4532015112830367", "1111", "abcd1234efgh5678"})
  void isValidCardNumber_returnsFalse_forInvalidNumbers(String cardNumber) {
    assertFalse(paymentService.isValidCardNumber(cardNumber));
  }

  @Test
  void isValidExpiryDate_returnsTrue_forFutureDate() {
    assertTrue(paymentService.isValidExpiryDate("12/30"));
  }

  @Test
  void isValidExpiryDate_returnsFalse_forPastDate() {
    assertFalse(paymentService.isValidExpiryDate("01/20"));
  }

  @Test
  void isValidExpiryDate_returnsFalse_forWrongFormat() {
    assertFalse(paymentService.isValidExpiryDate("13/25"));
    assertFalse(paymentService.isValidExpiryDate("2025-12"));
  }

  @Test
  void isValidCvv_returnsTrue_forThreeDigits() {
    assertTrue(paymentService.isValidCvv("123"));
  }

  @Test
  void isValidCvv_returnsFalse_forWrongLength() {
    assertFalse(paymentService.isValidCvv("12"));
    assertFalse(paymentService.isValidCvv("12345"));
  }
}