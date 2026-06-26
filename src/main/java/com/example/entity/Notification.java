package com.example.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

    @Column(columnDefinition="TEXT")
    private String message;

    @Builder.Default
    @Column(name="is_read")
    private Boolean isRead = false;

    @CreationTimestamp
    @Column(name="created_at",updatable=false)
    private LocalDateTime createdAt;
}