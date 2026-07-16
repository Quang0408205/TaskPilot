package com.example.repository;

import com.example.entity.Task;
import com.example.entity.User;
import com.example.entity.Project;
import com.example.enums.TaskStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task,Long>{

    List<Task> findByProject(Project project);

    List<Task> findByAssignedTo(User user);

    List<Task> findByStatus(TaskStatus status);

    List<Task> findByProjectAndDeletedFalseOrderByPositionAscCreatedAtDesc(Project project);

    List<Task> findByProjectInAndDeletedFalseOrderByUpdatedAtDesc(List<Project> projects);

    long countByProjectInAndDeletedFalse(List<Project> projects);

    long countByProjectInAndStatusAndDeletedFalse(List<Project> projects, TaskStatus status);

}
