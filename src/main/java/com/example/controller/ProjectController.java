package com.example.controller;

import com.example.dto.InviteRequest;
import com.example.dto.ProjectDto;
import com.example.dto.ProjectMemberResponse;
import com.example.dto.ProjectResponse;
import com.example.entity.Project;
import com.example.entity.ProjectMember;
import com.example.entity.User;
import com.example.repository.UserRepository;
import com.example.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getMyProjects(@AuthenticationPrincipal UserDetails currentUser) {
        User user = userRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
        List<Project> projects = projectService.getProjectsForUser(user);
        return ResponseEntity.ok(projects.stream().map(this::toResponse).collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@AuthenticationPrincipal UserDetails currentUser,
                                                         @RequestBody ProjectDto projectDto) {
        User owner = userRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
        Project project = projectService.createProject(projectDto, owner);
        return ResponseEntity.ok(toResponse(project));
    }

    @PostMapping("/{projectId}/invite")
    public ResponseEntity<ProjectResponse> inviteMember(@PathVariable Long projectId,
                                                        @RequestBody InviteRequest inviteRequest) {
        ProjectMember member = projectService.inviteMember(projectId, inviteRequest.getEmail());
        return ResponseEntity.ok(toResponse(member.getProject()));
    }

    private ProjectResponse toResponse(Project project) {
        List<ProjectMemberResponse> members = projectService.getProjectMembers(project).stream()
                .map(this::toMemberResponse)
                .collect(Collectors.toList());

        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getCreatedBy() != null ? project.getCreatedBy().getUsername() : null,
                members
        );
    }

    private ProjectMemberResponse toMemberResponse(ProjectMember member) {
        return new ProjectMemberResponse(
                member.getId(),
                member.getUser().getUsername(),
                member.getUser().getEmail(),
                member.getRole() != null ? member.getRole().name() : null
        );
    }
}
