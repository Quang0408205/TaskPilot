package com.example.service.impl;

import com.example.dto.ProjectDto;
import com.example.entity.Project;
import com.example.entity.ProjectMember;
import com.example.entity.User;
import com.example.repository.ProjectMemberRepository;
import com.example.repository.ProjectRepository;
import com.example.repository.UserRepository;
import com.example.repository.ActivityLogRepository;
import com.example.service.NotificationPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private UserRepository userRepository;

    @Mock private ActivityLogRepository activityLogRepository;
    @Mock private NotificationPublisher notificationPublisher;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private User owner;
    private Project project;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setEmail("owner@example.com");

        project = new Project();
        project.setId(10L);
        project.setName("Old name");
        project.setDescription("Old description");
        project.setCreatedBy(owner);
        project.setDeleted(false);
    }

    @Test
    void updateProject_shouldUpdateNameAndDescription_whenUserIsOwner() {
        ProjectDto dto = new ProjectDto();
        dto.setName("New name");
        dto.setDescription("New description");

        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Project updated = projectService.updateProject(10L, dto, owner);

        assertEquals("New name", updated.getName());
        assertEquals("New description", updated.getDescription());
        verify(projectRepository).save(project);
    }

    @Test
    void deleteProject_shouldMarkProjectAsDeleted_whenUserIsOwner() {
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        projectService.deleteProject(10L, owner);

        assertTrue(project.getDeleted());
        verify(projectRepository).save(project);
    }

    @Test
    void inviteMember_shouldCreatePendingMembership_whenOwnerInvitesExistingUser() {
        User invitedUser = new User();
        invitedUser.setId(2L);
        invitedUser.setEmail("member@example.com");

        ProjectMember pendingMember = new ProjectMember();
        pendingMember.setProject(project);
        pendingMember.setUser(invitedUser);
        pendingMember.setActive(false);

        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(userRepository.findByEmail("member@example.com")).thenReturn(Optional.of(invitedUser));
        when(projectMemberRepository.existsByProjectAndUser(project, invitedUser)).thenReturn(false);
        when(projectMemberRepository.save(any(ProjectMember.class))).thenReturn(pendingMember);

        ProjectMember result = projectService.inviteMember(10L, "member@example.com", owner);

        assertEquals(invitedUser.getId(), result.getUser().getId());
        assertFalse(result.isActive());
        verify(projectMemberRepository).save(any(ProjectMember.class));
    }

    @Test
    void joinProject_shouldActivateMembership_whenUserWasInvited() {
        User invitedUser = new User();
        invitedUser.setId(2L);
        invitedUser.setEmail("member@example.com");

        ProjectMember pendingMember = new ProjectMember();
        pendingMember.setProject(project);
        pendingMember.setUser(invitedUser);
        pendingMember.setActive(false);

        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.findByProjectAndUser(project, invitedUser)).thenReturn(Optional.of(pendingMember));
        when(projectMemberRepository.save(any(ProjectMember.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProjectMember result = projectService.joinProject(10L, invitedUser);

        assertTrue(result.isActive());
        assertEquals(invitedUser.getId(), result.getUser().getId());
        verify(projectMemberRepository).save(pendingMember);
    }
}
