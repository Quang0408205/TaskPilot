package com.example.dto;

import com.example.enums.TaskPriority;
import com.example.enums.TaskStatus;
import java.time.LocalDateTime;

public record TaskResponse(Long id, Long projectId, String projectName, String title, String description,
                           TaskStatus status, TaskPriority priority, LocalDateTime deadline,
                           Long assignedToId, String assignedToName, String createdByName,
                           LocalDateTime createdAt, LocalDateTime updatedAt) { }
