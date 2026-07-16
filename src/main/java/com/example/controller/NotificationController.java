package com.example.controller;

import com.example.dto.NotificationResponse;
import com.example.entity.Notification;
import com.example.entity.User;
import com.example.repository.NotificationRepository;
import com.example.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

@RestController @RequestMapping("/api/notifications") @RequiredArgsConstructor
public class NotificationController {
    private final NotificationRepository notificationRepository; private final UserRepository userRepository;
    @GetMapping public List<NotificationResponse> all(@AuthenticationPrincipal UserDetails principal) { User u = user(principal); return notificationRepository.findTop10ByUserIdOrderByCreatedAtDesc(u.getId()).stream().map(this::response).toList(); }
    @GetMapping("/unread-count") @Cacheable(value = "unread-count", key = "#principal.username") public long unread(@AuthenticationPrincipal UserDetails principal) { return notificationRepository.findByUserIdAndIsReadFalse(user(principal).getId()).size(); }
    @PutMapping("/{id}/read") @CacheEvict(value = "unread-count", key = "#principal.username") public NotificationResponse markRead(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal) { Notification n = notificationRepository.findById(id).filter(note -> note.getUser().getId().equals(user(principal).getId())).orElseThrow(() -> new RuntimeException("Notification not found")); n.setIsRead(true); return response(notificationRepository.save(n)); }
    @PutMapping("/read-all") @CacheEvict(value = "unread-count", key = "#principal.username") public ResponseEntity<Void> readAll(@AuthenticationPrincipal UserDetails principal) { notificationRepository.findByUserIdAndIsReadFalse(user(principal).getId()).forEach(n -> n.setIsRead(true)); notificationRepository.flush(); return ResponseEntity.noContent().build(); }
    private User user(UserDetails p) { return userRepository.findByEmail(p.getUsername()).orElseThrow(() -> new RuntimeException("Authenticated user not found")); }
    private NotificationResponse response(Notification n) { return new NotificationResponse(n.getId(), n.getMessage(), Boolean.TRUE.equals(n.getIsRead()), n.getCreatedAt()); }
}
