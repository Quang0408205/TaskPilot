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
    ProjectMember inviteMember(Long projectId, String email);
}
