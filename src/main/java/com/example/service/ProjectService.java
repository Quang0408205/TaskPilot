package com.example.service;

import com.example.dto.ProjectDto;
import com.example.entity.Project;
import com.example.entity.ProjectMember;
import com.example.entity.User;

import java.util.List;

public interface ProjectService {
    Project createProject(ProjectDto projectDto, User owner);
    Project getProjectById(Long id);
    List<Project> getProjectsForUser(User user);
    List<ProjectMember> getProjectMembers(Project project);
    Project updateProject(Long id, ProjectDto projectDto, User currentUser);
    void deleteProject(Long id, User currentUser);
    ProjectMember inviteMember(Long projectId, String email, User currentUser);
    ProjectMember joinProject(Long projectId, User currentUser);
}
