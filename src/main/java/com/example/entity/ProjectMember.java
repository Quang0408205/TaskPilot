package com.example.entity;

import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import com.example.enums.ProjectMemberRole;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table
(
    name = "project_members", 
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"project_id", "user_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="project_id")
    private Project project;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private ProjectMemberRole role;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @CreationTimestamp
    @Column(name="joined_at", updatable = false)
    private LocalDateTime joinedAt;

}
