package com.perebziak.musiceditor.service;

import com.perebziak.musiceditor.dto.RegisterRequest;
import com.perebziak.musiceditor.dto.VerifyCodeRequest;
import com.perebziak.musiceditor.exception.AuthException;
import com.perebziak.musiceditor.exception.ValidationException;
import com.perebziak.musiceditor.model.User;
import com.perebziak.musiceditor.model.ValidationResult;
import com.perebziak.musiceditor.model.VerificationCode;
import com.perebziak.musiceditor.repository.UserRepository;
import com.perebziak.musiceditor.repository.VerificationCodeRepository;
import com.perebziak.musiceditor.util.PasswordHasher;
import com.perebziak.musiceditor.util.ValidationUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private VerificationCodeRepository verificationCodeRepository;
  @Mock private EmailService emailService;
  @Mock private PasswordHasher passwordHasher;
  @Mock private ValidationUtil validationUtil;

  private AuthService authService;

  @BeforeEach
  void setUp() {
    authService = new AuthService(
        userRepository, verificationCodeRepository, emailService, passwordHasher, validationUtil);
  }

  @Test
  void initiateRegistration_savesCodeAndSendsEmail_whenDataIsValid() {
    RegisterRequest request = new RegisterRequest("newUser", "new@example.com", "Password123");

    when(validationUtil.validateRegistration(anyString(), anyString(), anyString()))
        .thenReturn(ValidationResult.valid());
    when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
    when(userRepository.existsByUsername("newUser")).thenReturn(false);
    when(passwordHasher.hash("Password123")).thenReturn("hashed-password");

    authService.initiateRegistration(request);

    verify(verificationCodeRepository, times(1)).save(any(VerificationCode.class));
    verify(emailService, times(1)).sendVerificationCode(eq("new@example.com"), anyString());
  }

  @Test
  void initiateRegistration_throwsValidationException_whenEmailAlreadyExists() {
    RegisterRequest request = new RegisterRequest("newUser", "taken@example.com", "Password123");

    when(validationUtil.validateRegistration(anyString(), anyString(), anyString()))
        .thenReturn(ValidationResult.valid());
    when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

    ValidationException exception = assertThrows(ValidationException.class,
        () -> authService.initiateRegistration(request));

    assertTrue(exception.getMessage().contains("вже зареєстрований"));
    verifyNoInteractions(emailService);
    verify(verificationCodeRepository, never()).save(any());
  }

  @Test
  void initiateRegistration_throwsValidationException_whenInputDataIsInvalid() {
    RegisterRequest request = new RegisterRequest("ab", "not-an-email", "123");

    when(validationUtil.validateRegistration(anyString(), anyString(), anyString()))
        .thenReturn(ValidationResult.invalid(List.of("Некоректний email", "Короткий пароль")));

    assertThrows(ValidationException.class, () -> authService.initiateRegistration(request));
    verifyNoInteractions(userRepository, emailService, verificationCodeRepository);
  }

  @Test
  void completeRegistration_createsUser_whenCodeIsCorrect() {
    String email = "new@example.com";
    RegisterRequest registerRequest = new RegisterRequest("newUser", email, "Password123");

    when(validationUtil.validateRegistration(anyString(), anyString(), anyString()))
        .thenReturn(ValidationResult.valid());
    when(userRepository.existsByEmail(email)).thenReturn(false);
    when(userRepository.existsByUsername("newUser")).thenReturn(false);
    when(passwordHasher.hash("Password123")).thenReturn("hashed-password");

    authService.initiateRegistration(registerRequest);

    VerificationCode savedCode = new VerificationCode();
    savedCode.setId(1L);
    savedCode.setEmail(email);
    savedCode.setCode("123456");
    savedCode.setExpiresDate(LocalDateTime.now().plusMinutes(10));

    when(validationUtil.isValidVerificationCode("123456")).thenReturn(true);
    when(verificationCodeRepository.findActiveByEmail(email)).thenReturn(Optional.of(savedCode));
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
      User u = invocation.getArgument(0);
      u.setId(42L);
      return u;
    });

    User result = authService.completeRegistration(new VerifyCodeRequest(email, "123456"));

    assertEquals("newUser", result.getUsername());
    assertEquals("hashed-password", result.getPasswordHash());
    assertEquals("USER", result.getRole());
    verify(verificationCodeRepository, times(1)).markAsUsed(1L);
  }

  @Test
  void completeRegistration_throwsAuthException_whenCodeIsExpiredOrMissing() {
    when(validationUtil.isValidVerificationCode("999999")).thenReturn(true);
    when(verificationCodeRepository.findActiveByEmail("someone@example.com"))
        .thenReturn(Optional.empty());

    assertThrows(AuthException.class, () -> authService.completeRegistration(
        new VerifyCodeRequest("someone@example.com", "999999")));

    verify(userRepository, never()).save(any());
  }

  @Test
  void completeRegistration_throwsAuthException_whenCodeDoesNotMatch() {
    String email = "user@example.com";
    VerificationCode savedCode = new VerificationCode();
    savedCode.setId(2L);
    savedCode.setEmail(email);
    savedCode.setCode("111111");
    savedCode.setExpiresDate(LocalDateTime.now().plusMinutes(5));

    when(validationUtil.isValidVerificationCode("000000")).thenReturn(true);
    when(verificationCodeRepository.findActiveByEmail(email)).thenReturn(Optional.of(savedCode));

    assertThrows(AuthException.class, () -> authService.completeRegistration(
        new VerifyCodeRequest(email, "000000")));

    verify(verificationCodeRepository, never()).markAsUsed(any());
  }

  @Test
  void login_returnsUser_whenCredentialsAreCorrect() {
    User user = new User();
    user.setId(1L);
    user.setUsername("takilas");
    user.setPasswordHash("hashed-password");

    when(userRepository.findByUsername("takilas")).thenReturn(Optional.of(user));
    when(passwordHasher.matches("correct-password", "hashed-password")).thenReturn(true);

    User result = authService.login("takilas", "correct-password");

    assertEquals("takilas", result.getUsername());
  }

  @Test
  void login_throwsAuthException_whenPasswordIsWrong() {
    User user = new User();
    user.setUsername("takilas");
    user.setPasswordHash("hashed-password");

    when(userRepository.findByUsername("takilas")).thenReturn(Optional.of(user));
    when(passwordHasher.matches("wrong-password", "hashed-password")).thenReturn(false);

    assertThrows(AuthException.class, () -> authService.login("takilas", "wrong-password"));
  }

  @Test
  void login_throwsAuthException_whenUserDoesNotExist() {
    when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());
    when(userRepository.findByEmail("ghost")).thenReturn(Optional.empty());

    assertThrows(AuthException.class, () -> authService.login("ghost", "anyPassword"));
  }
}