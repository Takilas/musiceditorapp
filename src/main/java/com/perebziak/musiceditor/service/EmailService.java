package com.perebziak.musiceditor.service;

import com.perebziak.musiceditor.exception.EmailException;
import com.perebziak.musiceditor.util.AppConfig;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailService {

  private final AppConfig config;

  public EmailService(AppConfig config) {
    this.config = config;
  }

  public void sendVerificationCode(String toEmail, String code) {
    Properties props = new Properties();
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.starttls.enable", "true");
    props.put("mail.smtp.host", config.getSmtpHost());
    props.put("mail.smtp.port", String.valueOf(config.getSmtpPort()));
    // Таймаути, щоб при проблемах з мережею/автентифікацією
    // помилка виникала швидко (5 сек), а не "висіла" довго
    props.put("mail.smtp.connectiontimeout", "5000");
    props.put("mail.smtp.timeout", "5000");
    props.put("mail.smtp.writetimeout", "5000");

    Session session = Session.getInstance(props, new Authenticator() {
      @Override
      protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(config.getSmtpUsername(), config.getSmtpAppPassword());
      }
    });

    try {
      Message message = new MimeMessage(session);
      message.setFrom(new InternetAddress(config.getSmtpUsername()));
      message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
      message.setSubject("Код підтвердження реєстрації — Музичний редактор");
      message.setText("Ваш код підтвердження: " + code + "\n\nКод дійсний протягом 10 хвилин.");
      Transport.send(message);
    } catch (MessagingException e) {
      throw new EmailException(
          "Не вдалося надіслати лист на " + toEmail + ". Перевірте налаштування пошти (email.properties).",
          e);
    }
  }
}