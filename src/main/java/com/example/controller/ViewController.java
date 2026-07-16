package com.example.controller;

import com.example.entity.User;
import com.example.repository.UserRepository;
import com.example.repository.NotificationRepository;
import com.example.repository.TaskRepository;
import com.example.repository.ActivityLogRepository;
import com.example.service.ProjectService;
import com.example.entity.Project;
import com.example.enums.TaskStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ViewController {

    private final UserRepository userRepository;
    private final ProjectService projectService;
    private final TaskRepository taskRepository;
    private final NotificationRepository notificationRepository;
    private final ActivityLogRepository activityLogRepository;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails currentUser, Model model) {
        if (currentUser != null) {
            User user = userRepository.findByEmail(currentUser.getUsername())
                    .orElse(null);
            if (user != null) {
                model.addAttribute("user", new UserDTO(user));
                List<Project> projects = projectService.getProjectsForUser(user);
                long totalTasks = taskRepository.countByProjectInAndDeletedFalse(projects);
                long completed = taskRepository.countByProjectInAndStatusAndDeletedFalse(projects, TaskStatus.DONE);
                long members = projects.stream().flatMap(p -> projectService.getProjectMembers(p).stream()).map(m -> m.getUser().getId()).distinct().count();
                model.addAttribute("notificationCount", notificationRepository.findByUserIdAndIsReadFalse(user.getId()).size());
                model.addAttribute("recentProjects", projects.stream().limit(5).map(p -> Map.of("name", p.getName(), "description", p.getDescription() == null ? "No description" : p.getDescription(), "status", "Active", "dueDate", p.getEndDate() == null ? "—" : p.getEndDate().toString(), "progress", 0, "taskCount", taskRepository.findByProjectAndDeletedFalseOrderByPositionAscCreatedAtDesc(p).size())).toList());
                model.addAttribute("recentTasks", taskRepository.findByProjectInAndDeletedFalseOrderByUpdatedAtDesc(projects).stream().limit(8).map(t -> Map.of("title", t.getTitle(), "assigneeInitials", t.getAssignedTo() == null ? "—" : t.getAssignedTo().getUsername().substring(0, 1).toUpperCase(), "assigneeName", t.getAssignedTo() == null ? "Unassigned" : t.getAssignedTo().getUsername(), "priority", t.getPriority().name(), "status", t.getStatus().name(), "dueDate", t.getDeadline() == null ? "—" : t.getDeadline().toLocalDate().toString())).toList());
                model.addAttribute("teamMembers", projects.stream().flatMap(p -> projectService.getProjectMembers(p).stream()).collect(java.util.stream.Collectors.toMap(m -> m.getUser().getId(), m -> m, (a,b) -> a)).values().stream().map(m -> Map.of("initials", m.getUser().getUsername().substring(0, 1).toUpperCase(), "name", m.getUser().getUsername(), "email", m.getUser().getEmail(), "role", m.getRole().name(), "taskCount", taskRepository.findByAssignedTo(m.getUser()).size())).toList());
                model.addAttribute("notifications", notificationRepository.findTop10ByUserIdOrderByCreatedAtDesc(user.getId()).stream().map(n -> Map.of("title", "Notification", "subtitle", n.getMessage(), "time", n.getCreatedAt() == null ? "now" : n.getCreatedAt().toString())).toList());
                model.addAttribute("activityTimeline", activityLogRepository.findTop20ByProjectInOrderByCreatedAtDesc(projects).stream().map(a -> Map.of("message", a.getMessage(), "time", a.getCreatedAt() == null ? "now" : a.getCreatedAt().toString())).toList());
                Map<String, Integer> summary = new HashMap<>();
                summary.put("totalProjects", projects.size()); summary.put("totalTasks", (int) totalTasks);
                summary.put("completedTasks", (int) completed); summary.put("pendingTasks", (int) (totalTasks - completed)); summary.put("teamMembers", (int) members);
                model.addAttribute("summary", summary);
                Map<String, Object> analytics = new HashMap<>();
                analytics.put("tasksThisWeek", completed); analytics.put("projectProgress", totalTasks == 0 ? "0%" : Math.round(completed * 100.0 / totalTasks) + "%"); analytics.put("statusBreakdown", taskRepository.countByProjectInAndStatusAndDeletedFalse(projects, TaskStatus.TODO) + " / " + taskRepository.countByProjectInAndStatusAndDeletedFalse(projects, TaskStatus.IN_PROGRESS) + " / " + completed); analytics.put("productivity", totalTasks == 0 ? "0%" : Math.round(completed * 100.0 / totalTasks) + "%");
                model.addAttribute("analytics", analytics);
            }
        }
        return "dashboard";
    }

    @GetMapping("/projects")
    public String projects(@AuthenticationPrincipal UserDetails currentUser, Model model) {
        if (currentUser != null) {
            User user = userRepository.findByEmail(currentUser.getUsername())
                    .orElse(null);
            if (user != null) {
                model.addAttribute("user", new UserDTO(user));
                model.addAttribute("notificationCount", 0);
            }
        }
        return "projects";
    }

    @GetMapping("/tasks")
    public String tasks(@AuthenticationPrincipal UserDetails currentUser, Model model) {
        if (currentUser != null) userRepository.findByEmail(currentUser.getUsername()).ifPresent(user -> model.addAttribute("user", new UserDTO(user)));
        return "tasks";
    }

    @GetMapping("/error")
    public String error() {
        return "error";
    }

    // DTO for User to safely expose data
    static class UserDTO {
        public String username;
        public String email;
        public String initials;
        public String role;

        public UserDTO(User user) {
            this.username = user.getUsername();
            this.email = user.getEmail();
            this.initials = user.getUsername().substring(0, 1).toUpperCase();
            this.role = "OWNER";
        }
    }
}
