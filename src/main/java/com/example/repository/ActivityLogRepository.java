package com.example.repository;

import com.example.entity.ActivityLog;
import com.example.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    List<ActivityLog> findTop20ByProjectInOrderByCreatedAtDesc(Collection<Project> projects);
}
