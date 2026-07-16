package com.example.service;

import com.example.entity.Notification;
import com.example.entity.User;
import com.example.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.CacheEvict;

@Service @RequiredArgsConstructor
public class NotificationPublisher {
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final JavaMailSender mailSender;
    @Value("${app.mail.enabled:false}") private boolean emailEnabled;

    @CacheEvict(value = "unread-count", key = "#user.email")
    public Notification notify(User user, String message) {
        Notification saved = notificationRepository.save(Notification.builder().user(user).message(message).build());
        messagingTemplate.convertAndSendToUser(user.getEmail(), "/queue/notifications", message);
        if (emailEnabled) sendEmail(user.getEmail(), "TaskPilot notification", message);
        return saved;
    }
    @Async void sendEmail(String recipient, String subject, String text) { try { SimpleMailMessage mail = new SimpleMailMessage(); mail.setTo(recipient); mail.setSubject(subject); mail.setText(text); mailSender.send(mail); } catch (Exception ignored) { } }
}
