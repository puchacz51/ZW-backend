package pl.pbs.zwbackend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.pbs.zwbackend.dto.ProjectRequest;
import pl.pbs.zwbackend.dto.ProjectResponse;
import pl.pbs.zwbackend.dto.ProjectUserResponse;
import pl.pbs.zwbackend.dto.UserSummaryResponse;
import pl.pbs.zwbackend.exception.ResourceNotFoundException;
import pl.pbs.zwbackend.exception.UnauthorizedOperationException;
import pl.pbs.zwbackend.model.Project;
import pl.pbs.zwbackend.model.User;
import pl.pbs.zwbackend.model.enums.ProjectStatus;
import pl.pbs.zwbackend.repository.ProjectRepository;
import pl.pbs.zwbackend.repository.ProjectUserRepository;
import pl.pbs.zwbackend.repository.ProjectCommentRepository;
import pl.pbs.zwbackend.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final ProjectUserRepository projectUserRepository;
    private final ProjectCommentRepository projectCommentRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public ProjectResponse createProject(ProjectRequest projectRequest, String userEmail) {
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        Project project = Project.builder()
                .name(projectRequest.getName())
                .description(projectRequest.getDescription())
                .startDate(projectRequest.getStartDate())
                .endDate(projectRequest.getEndDate())
                .status(projectRequest.getStatus() != null ? projectRequest.getStatus() : ProjectStatus.NOT_STARTED)
                .createdBy(currentUser)
                .build();

        Project savedProject = projectRepository.save(project);
        
        // Logowanie audytu
        auditLogService.logActionAsync(userEmail, AuditLogService.ACTION_CREATE, 
                AuditLogService.ENTITY_PROJECT, savedProject.getId());
        
        return convertToResponse(savedProject);
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
        return convertToResponse(project);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjectsCreatedByUser(String userEmail) {
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));
        return projectRepository.findByCreatedBy(currentUser).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProjectResponse updateProject(Long projectId, ProjectRequest projectRequest, String userEmail) {
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        if (!project.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new UnauthorizedOperationException("User not authorized to update this project");
        }

        project.setName(projectRequest.getName());
        project.setDescription(projectRequest.getDescription());
        project.setStartDate(projectRequest.getStartDate());
        project.setEndDate(projectRequest.getEndDate());
        if (projectRequest.getStatus() != null) {
            project.setStatus(projectRequest.getStatus());
        }

        Project updatedProject = projectRepository.save(project);
        
        // Logowanie audytu
        auditLogService.logActionAsync(userEmail, AuditLogService.ACTION_UPDATE, 
                AuditLogService.ENTITY_PROJECT, projectId);
        
        return convertToResponse(updatedProject);
    }

    @Transactional
    public void deleteProject(Long projectId, String userEmail) {
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        if (!project.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new UnauthorizedOperationException("User not authorized to delete this project");
        }
        
        // Logowanie audytu przed usunięciem
        auditLogService.logAction(userEmail, AuditLogService.ACTION_DELETE, 
                AuditLogService.ENTITY_PROJECT, projectId);
        
        // Kaskadowe usuwanie jest ustawione w encji Project
        projectRepository.delete(project);
    }

    private ProjectResponse convertToResponse(Project project) {
        UserSummaryResponse userSummary = userService.convertToUserSummaryResponse(project.getCreatedBy());
        
        // Get assigned users
        List<ProjectUserResponse> assignedUsers = projectUserRepository.findByProjectIdWithUsers(project.getId())
                .stream()
                .map(pu -> ProjectUserResponse.builder()
                        .user(userService.convertToUserSummaryResponse(pu.getUser()))
                        .role(pu.getRole())
                        .build())
                .collect(Collectors.toList());

        // Get comment count
        Long commentCount = projectCommentRepository.countByProjectId(project.getId());

        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .status(project.getStatus())
                .createdBy(userSummary)
                .createdAt(project.getCreatedAt())
                .assignedUsers(assignedUsers)
                .commentCount(commentCount)
                .build();
    }
}
