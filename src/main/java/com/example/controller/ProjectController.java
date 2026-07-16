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
        User user = getAuthenticatedUser(currentUser);
        List<Project> projects = projectService.getProjectsForUser(user);
        return ResponseEntity.ok(projects.stream().map(this::toResponse).collect(Collectors.toList()));
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> getProject(@AuthenticationPrincipal UserDetails currentUser,
                                                     @PathVariable Long projectId) {
        User user = getAuthenticatedUser(currentUser);
        Project project = projectService.getProjectById(projectId);
        if (!projectService.getProjectMembers(project).stream().anyMatch(member -> member.getUser().getId().equals(user.getId())))
            throw new RuntimeException("You are not allowed to view this project");
        return ResponseEntity.ok(toResponse(project));
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@AuthenticationPrincipal UserDetails currentUser,
                                                         @RequestBody ProjectDto projectDto) {
        User owner = getAuthenticatedUser(currentUser);
        Project project = projectService.createProject(projectDto, owner);
        return ResponseEntity.ok(toResponse(project));
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> updateProject(@AuthenticationPrincipal UserDetails currentUser,
                                                         @PathVariable Long projectId,
                                                         @RequestBody ProjectDto projectDto) {
        User currentUserEntity = getAuthenticatedUser(currentUser);
        Project project = projectService.updateProject(projectId, projectDto, currentUserEntity);
        return ResponseEntity.ok(toResponse(project));
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(@AuthenticationPrincipal UserDetails currentUser,
                                               @PathVariable Long projectId) {
        User currentUserEntity = getAuthenticatedUser(currentUser);
        projectService.deleteProject(projectId, currentUserEntity);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{projectId}/invite")
    public ResponseEntity<ProjectResponse> inviteMember(@AuthenticationPrincipal UserDetails currentUser,
                                                        @PathVariable Long projectId,
                                                        @RequestBody InviteRequest inviteRequest) {
        User inviter = getAuthenticatedUser(currentUser);
        ProjectMember member = projectService.inviteMember(projectId, inviteRequest.getEmail(), inviter);
        return ResponseEntity.ok(toResponse(member.getProject()));
    }

    @PostMapping("/{projectId}/join")
    public ResponseEntity<ProjectResponse> joinProject(@AuthenticationPrincipal UserDetails currentUser,
                                                       @PathVariable Long projectId) {
        User member = getAuthenticatedUser(currentUser);
        ProjectMember projectMember = projectService.joinProject(projectId, member);
        return ResponseEntity.ok(toResponse(projectMember.getProject()));
    }

    private User getAuthenticatedUser(UserDetails currentUser) {
        return userRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
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
                member.getUser().getId(),
                member.getUser().getUsername(),
                member.getUser().getEmail(),
                member.getRole() != null ? member.getRole().name() : null
        );
    }
}
