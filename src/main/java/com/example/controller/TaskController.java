package com.example.controller;

import com.example.dto.*;
import com.example.entity.User;
import com.example.repository.UserRepository;
import com.example.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/tasks") @RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService; private final UserRepository userRepository;
    @GetMapping public List<TaskResponse> getTasks(@RequestParam(required = false) Long projectId, @AuthenticationPrincipal UserDetails principal) { return taskService.getTasks(projectId, user(principal)); }
    @PostMapping public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskRequest body, @AuthenticationPrincipal UserDetails principal) { return ResponseEntity.status(HttpStatus.CREATED).body(taskService.create(body, user(principal))); }
    @PutMapping("/{id}") public TaskResponse update(@PathVariable Long id, @Valid @RequestBody TaskRequest body, @AuthenticationPrincipal UserDetails principal) { return taskService.update(id, body, user(principal)); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal) { taskService.delete(id, user(principal)); return ResponseEntity.noContent().build(); }
    @GetMapping("/{id}/comments") public List<CommentResponse> comments(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal) { return taskService.getComments(id, user(principal)); }
    @PostMapping("/{id}/comments") public ResponseEntity<CommentResponse> comment(@PathVariable Long id, @Valid @RequestBody CommentRequest body, @AuthenticationPrincipal UserDetails principal) { return ResponseEntity.status(HttpStatus.CREATED).body(taskService.addComment(id, body, user(principal))); }
    @GetMapping("/{id}/history") public List<String> history(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal) { return taskService.getHistory(id, user(principal)); }
    private User user(UserDetails p) { return userRepository.findByEmail(p.getUsername()).orElseThrow(() -> new RuntimeException("Authenticated user not found")); }
}
