package com.example.controller;

import com.example.entity.User;
import com.example.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ViewController {

    private final UserRepository userRepository;

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
                model.addAttribute("notificationCount", 0);
                model.addAttribute("recentProjects", new java.util.ArrayList<>());
                model.addAttribute("recentTasks", new java.util.ArrayList<>());
                model.addAttribute("teamMembers", new java.util.ArrayList<>());
                model.addAttribute("notifications", new java.util.ArrayList<>());
                model.addAttribute("activityTimeline", new java.util.ArrayList<>());
                
                Map<String, Integer> summary = new HashMap<>();
                summary.put("totalProjects", 0);
                summary.put("totalTasks", 0);
                summary.put("completedTasks", 0);
                summary.put("pendingTasks", 0);
                summary.put("teamMembers", 0);
                model.addAttribute("summary", summary);
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
