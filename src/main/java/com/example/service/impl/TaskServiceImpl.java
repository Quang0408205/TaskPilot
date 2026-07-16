package com.example.service.impl;

import com.example.dto.*;
import com.example.entity.*;
import com.example.enums.*;
import com.example.repository.*;
import com.example.service.ProjectService;
import com.example.service.TaskService;
import com.example.service.NotificationPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service @RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;
    private final ProjectService projectService;
    private final ProjectMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final NotificationPublisher notificationPublisher;
    private final TaskHistoryRepository historyRepository;
    private final ActivityLogRepository activityLogRepository;

    // Legacy methods retained for compatibility; application APIs below enforce membership.
    public Task createTask(Task task) { return taskRepository.save(task); }
    public List<Task> getAllTasks() { return taskRepository.findAll(); }
    public Task getTaskById(Long id) { return taskRepository.findById(id).filter(t -> !Boolean.TRUE.equals(t.getDeleted())).orElseThrow(() -> new RuntimeException("Task not found")); }
    public Task updateTask(Long id, Task task) { Task current = getTaskById(id); current.setTitle(task.getTitle()); current.setDescription(task.getDescription()); return taskRepository.save(current); }
    public void deleteTask(Long id) { Task task = getTaskById(id); task.setDeleted(true); taskRepository.save(task); }

    @Transactional public TaskResponse create(TaskRequest request, User currentUser) {
        Project project = projectService.getProjectById(request.getProjectId());
        ensureMember(project, currentUser);
        Task task = new Task(); task.setProject(project); task.setTitle(request.getTitle().trim()); task.setDescription(request.getDescription());
        task.setStatus(request.getStatus() == null ? TaskStatus.TODO : request.getStatus());
        task.setPriority(request.getPriority() == null ? TaskPriority.MEDIUM : request.getPriority());
        task.setDeadline(request.getDeadline()); task.setCreatedBy(currentUser);
        applyAssignee(task, request.getAssignedToId(), project);
        task = taskRepository.save(task);
        record(task, currentUser, TaskActionType.CREATED, currentUser.getUsername() + " created task \"" + task.getTitle() + "\"");
        if (task.getAssignedTo() != null) notifyUser(task.getAssignedTo(), "You were assigned task \"" + task.getTitle() + "\" in " + project.getName());
        return toResponse(task);
    }

    public List<TaskResponse> getTasks(Long projectId, User currentUser) {
        if (projectId != null) { Project p = projectService.getProjectById(projectId); ensureMember(p, currentUser); return taskRepository.findByProjectAndDeletedFalseOrderByPositionAscCreatedAtDesc(p).stream().map(this::toResponse).toList(); }
        List<Project> projects = projectService.getProjectsForUser(currentUser);
        return taskRepository.findByProjectInAndDeletedFalseOrderByUpdatedAtDesc(projects).stream().map(this::toResponse).toList();
    }

    @Transactional public TaskResponse update(Long id, TaskRequest request, User currentUser) {
        Task task = getTaskById(id); ensureMember(task.getProject(), currentUser); ensureCanEdit(task, currentUser);
        String oldAssignee = task.getAssignedTo() == null ? null : task.getAssignedTo().getUsername();
        TaskStatus oldStatus = task.getStatus();
        task.setTitle(request.getTitle().trim()); task.setDescription(request.getDescription()); task.setPriority(request.getPriority() == null ? task.getPriority() : request.getPriority()); task.setDeadline(request.getDeadline());
        if (request.getStatus() != null) task.setStatus(request.getStatus());
        applyAssignee(task, request.getAssignedToId(), task.getProject()); task = taskRepository.save(task);
        record(task, currentUser, TaskActionType.UPDATED, currentUser.getUsername() + " updated task \"" + task.getTitle() + "\"");
        if (oldStatus != task.getStatus()) record(task, currentUser, TaskActionType.STATUS_CHANGED, currentUser.getUsername() + " changed status to " + task.getStatus());
        if (task.getAssignedTo() != null && !task.getAssignedTo().getUsername().equals(oldAssignee)) { record(task, currentUser, TaskActionType.ASSIGNED, currentUser.getUsername() + " assigned " + task.getAssignedTo().getUsername()); notifyUser(task.getAssignedTo(), "You were assigned task \"" + task.getTitle() + "\" in " + task.getProject().getName()); }
        return toResponse(task);
    }

    @Transactional public void delete(Long id, User currentUser) { Task task = getTaskById(id); ensureMember(task.getProject(), currentUser); ensureManager(task.getProject(), currentUser); task.setDeleted(true); taskRepository.save(task); record(task, currentUser, TaskActionType.UPDATED, currentUser.getUsername() + " deleted task \"" + task.getTitle() + "\""); }

    public List<CommentResponse> getComments(Long taskId, User currentUser) { Task task = getTaskById(taskId); ensureMember(task.getProject(), currentUser); return commentRepository.findByTaskId(taskId).stream().map(c -> new CommentResponse(c.getId(), c.getContent(), c.getUser().getUsername(), c.getCreatedAt())).toList(); }
    @Transactional public CommentResponse addComment(Long taskId, CommentRequest request, User currentUser) { Task task = getTaskById(taskId); ensureMember(task.getProject(), currentUser); Comment c = Comment.builder().task(task).user(currentUser).content(request.getContent().trim()).build(); c = commentRepository.save(c); record(task, currentUser, TaskActionType.COMMENT_ADDED, currentUser.getUsername() + " commented on \"" + task.getTitle() + "\""); if (task.getAssignedTo() != null && !task.getAssignedTo().getId().equals(currentUser.getId())) notifyUser(task.getAssignedTo(), currentUser.getUsername() + " commented on task \"" + task.getTitle() + "\""); return new CommentResponse(c.getId(), c.getContent(), currentUser.getUsername(), c.getCreatedAt()); }
    public List<String> getHistory(Long taskId, User currentUser) { Task task = getTaskById(taskId); ensureMember(task.getProject(), currentUser); return historyRepository.findByTaskIdOrderByChangedAtDesc(taskId).stream().map(TaskHistory::getDescription).toList(); }

    private void applyAssignee(Task task, Long userId, Project project) { if (userId == null) { task.setAssignedTo(null); return; } User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Assignee not found")); ensureMember(project, user); task.setAssignedTo(user); }
    private void ensureMember(Project p, User u) { ProjectMember m = memberRepository.findByProjectAndUser(p, u).orElseThrow(() -> new RuntimeException("You are not a member of this project")); if (!m.isActive()) throw new RuntimeException("Your invitation has not been accepted"); }
    private void ensureManager(Project p, User u) { if (p.getCreatedBy() == null || !p.getCreatedBy().getId().equals(u.getId())) throw new RuntimeException("Only the project owner can do this"); }
    private void ensureCanEdit(Task t, User u) { if (t.getCreatedBy().getId().equals(u.getId()) || (t.getAssignedTo() != null && t.getAssignedTo().getId().equals(u.getId())) || t.getProject().getCreatedBy().getId().equals(u.getId())) return; throw new RuntimeException("You cannot edit this task"); }
    private void notifyUser(User user, String message) { notificationPublisher.notify(user, message); }
    private void record(Task task, User user, TaskActionType type, String message) { historyRepository.save(TaskHistory.builder().task(task).user(user).actionType(type).description(message).build()); activityLogRepository.save(ActivityLog.builder().project(task.getProject()).user(user).message(message).build()); }
    private TaskResponse toResponse(Task t) { return new TaskResponse(t.getId(), t.getProject().getId(), t.getProject().getName(), t.getTitle(), t.getDescription(), t.getStatus(), t.getPriority(), t.getDeadline(), t.getAssignedTo() == null ? null : t.getAssignedTo().getId(), t.getAssignedTo() == null ? null : t.getAssignedTo().getUsername(), t.getCreatedBy() == null ? null : t.getCreatedBy().getUsername(), t.getCreatedAt(), t.getUpdatedAt()); }
}
