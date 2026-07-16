package com.example.service.impl;

import com.example.dto.ProjectDto;
import com.example.entity.Project;
import com.example.entity.ProjectMember;
import com.example.entity.User;
import com.example.enums.ProjectMemberRole;
import com.example.repository.ProjectMemberRepository;
import com.example.repository.ProjectRepository;
import com.example.repository.UserRepository;
import com.example.repository.ActivityLogRepository;
import com.example.entity.ActivityLog;
import com.example.service.NotificationPublisher;
import com.example.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final NotificationPublisher notificationPublisher;
    private final ActivityLogRepository activityLogRepository;

    @Override
    @Transactional
    public Project createProject(ProjectDto projectDto, User owner) {
        Project project = new Project();
        project.setName(projectDto.getName());
        project.setDescription(projectDto.getDescription());
        project.setCreatedBy(owner);
        project = projectRepository.save(project);

        ProjectMember ownerMember = new ProjectMember();
        ownerMember.setProject(project);
        ownerMember.setUser(owner);
        ownerMember.setRole(ProjectMemberRole.ADMIN);
        projectMemberRepository.save(ownerMember);

        return project;
    }

    @Override
    public Project getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        if (Boolean.TRUE.equals(project.getDeleted())) {
            throw new RuntimeException("Project not found");
        }
        return project;
    }

    @Override
    public List<Project> getProjectsForUser(User user) {
        return projectMemberRepository.findByUser(user).stream()
                .filter(ProjectMember::isActive)
                .map(ProjectMember::getProject)
                .filter(project -> !Boolean.TRUE.equals(project.getDeleted()))
                .distinct()
                .toList();
    }

    @Override
    public List<ProjectMember> getProjectMembers(Project project) {
        return projectMemberRepository.findByProject(project).stream()
                .filter(ProjectMember::isActive)
                .toList();
    }

    @Override
    @Transactional
    public Project updateProject(Long id, ProjectDto projectDto, User currentUser) {
        Project project = getProjectById(id);
        ensureCanManage(project, currentUser);

        project.setName(projectDto.getName());
        project.setDescription(projectDto.getDescription());
        return projectRepository.save(project);
    }

    @Override
    @Transactional
    public void deleteProject(Long id, User currentUser) {
        Project project = getProjectById(id);
        ensureCanManage(project, currentUser);

        project.setDeleted(true);
        projectRepository.save(project);
    }

    @Override
    @Transactional
    public ProjectMember inviteMember(Long projectId, String email, User currentUser) {
        Project project = getProjectById(projectId);
        ensureCanInvite(project, currentUser);

        User invited = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        if (projectMemberRepository.existsByProjectAndUser(project, invited)) {
            ProjectMember existingMember = projectMemberRepository.findByProjectAndUser(project, invited)
                    .orElseThrow(() -> new RuntimeException("User is already a member of this project"));
            if (existingMember.isActive()) {
                throw new RuntimeException("User is already a member of this project");
            }
            existingMember.setActive(false);
            ProjectMember saved = projectMemberRepository.save(existingMember);
            notificationPublisher.notify(invited, currentUser.getUsername() + " invited you to join project \"" + project.getName() + "\"");
            activityLogRepository.save(ActivityLog.builder().project(project).user(currentUser)
                    .message(currentUser.getUsername() + " re-invited " + invited.getUsername() + " to the project").build());
            return saved;
        }

        ProjectMember member = new ProjectMember();
        member.setProject(project);
        member.setUser(invited);
        member.setRole(ProjectMemberRole.MEMBER);
        member.setActive(false);
        member = projectMemberRepository.save(member);
        notificationPublisher.notify(invited, currentUser.getUsername() + " invited you to join project \"" + project.getName() + "\"");
        activityLogRepository.save(ActivityLog.builder().project(project).user(currentUser)
                .message(currentUser.getUsername() + " invited " + invited.getUsername() + " to the project").build());
        return member;
    }

    @Override
    @Transactional
    public ProjectMember joinProject(Long projectId, User currentUser) {
        Project project = getProjectById(projectId);
        ProjectMember membership = projectMemberRepository.findByProjectAndUser(project, currentUser)
                .orElseThrow(() -> new RuntimeException("You do not have an invitation to this project"));

        if (membership.isActive()) {
            return membership;
        }

        membership.setActive(true);
        return projectMemberRepository.save(membership);
    }

    private void ensureCanManage(Project project, User currentUser) {
        if (currentUser == null || project.getCreatedBy() == null || !currentUser.getId().equals(project.getCreatedBy().getId())) {
            throw new RuntimeException("You are not allowed to manage this project");
        }
    }

    private void ensureCanInvite(Project project, User currentUser) {
        ensureCanManage(project, currentUser);
    }
}
