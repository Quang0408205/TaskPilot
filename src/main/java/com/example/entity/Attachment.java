package com.example.entity;

import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "attachments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="task_id")
    private Task task;

    @Column(name="file_name", nullable=false)
    private String fileName;

    @Column(name="file_url", nullable=false)
    private String fileUrl;

    private Long fileSize;

    @Column(name="file_type")
    private String fileType;

    @ManyToOne
    @JoinColumn(name="uploaded_by")
    private User uploadedBy;

    @CreationTimestamp
    @Column(name="created_at", updatable=false)
    private LocalDateTime createdAt;
}