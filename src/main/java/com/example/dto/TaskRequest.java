package com.example.dto;

import com.example.enums.TaskPriority;
import com.example.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class TaskRequest {
    @NotNull private Long projectId;
    @NotBlank private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDateTime deadline;
    private Long assignedToId;
}
