package com.example.service;

import com.example.entity.Task;
import com.example.entity.User;
import com.example.dto.*;
import java.util.List;

public interface TaskService {
    Task createTask(Task task);
    List<Task> getAllTasks();
    Task getTaskById(Long id);
    Task updateTask(Long id, Task task);
    void deleteTask(Long id);
    TaskResponse create(TaskRequest request, User currentUser);
    List<TaskResponse> getTasks(Long projectId, User currentUser);
    TaskResponse update(Long id, TaskRequest request, User currentUser);
    void delete(Long id, User currentUser);
    List<CommentResponse> getComments(Long taskId, User currentUser);
    CommentResponse addComment(Long taskId, CommentRequest request, User currentUser);
    List<String> getHistory(Long taskId, User currentUser);
}
