package pl.pbs.zwbackend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.pbs.zwbackend.dto.TaskCommentRequest;
import pl.pbs.zwbackend.dto.TaskCommentResponse;
import pl.pbs.zwbackend.exception.ResourceNotFoundException;
import pl.pbs.zwbackend.exception.UnauthorizedOperationException;
import pl.pbs.zwbackend.model.Task;
import pl.pbs.zwbackend.model.TaskComment;
import pl.pbs.zwbackend.model.User;
import pl.pbs.zwbackend.repository.TaskCommentRepository;
import pl.pbs.zwbackend.repository.TaskRepository;
import pl.pbs.zwbackend.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskCommentService {

    private final TaskCommentRepository taskCommentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    @Transactional
    public TaskCommentResponse createComment(TaskCommentRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));
        
        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", request.getTaskId()));

        TaskComment comment = TaskComment.builder()
                .task(task)
                .user(user)
                .content(request.getContent())
                .build();

        TaskComment savedComment = taskCommentRepository.save(comment);

        // Logowanie audytu
        auditLogService.logActionAsync(userEmail, AuditLogService.ACTION_CREATE, 
                "TaskComment", savedComment.getId());

        // Powiadomienie dla przypisanej osoby (jeśli to nie autor komentarza)
        if (task.getAssignedTo() != null && !task.getAssignedTo().getId().equals(user.getId())) {
            notificationService.notifyNewComment(
                    task.getAssignedTo().getId(),
                    user.getFullName(),
                    "zadaniu '" + task.getName() + "'"
            );
        }

        return convertToResponse(savedComment);
    }

    @Transactional(readOnly = true)
    public List<TaskCommentResponse> getCommentsForTask(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Task", "id", taskId);
        }
        
        return taskCommentRepository.findByTaskIdWithUsers(taskId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TaskCommentResponse getCommentById(Long commentId) {
        TaskComment comment = taskCommentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("TaskComment", "id", commentId));
        return convertToResponse(comment);
    }

    @Transactional
    public TaskCommentResponse updateComment(Long commentId, String newContent, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));
        
        TaskComment comment = taskCommentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("TaskComment", "id", commentId));

        // Sprawdzenie czy użytkownik jest autorem komentarza
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedOperationException("Tylko autor może edytować komentarz");
        }

        comment.setContent(newContent);
        comment.setUpdatedAt(LocalDateTime.now());
        TaskComment updatedComment = taskCommentRepository.save(comment);

        // Logowanie audytu
        auditLogService.logActionAsync(userEmail, AuditLogService.ACTION_UPDATE, 
                "TaskComment", commentId);

        return convertToResponse(updatedComment);
    }

    @Transactional
    public void deleteComment(Long commentId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));
        
        TaskComment comment = taskCommentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("TaskComment", "id", commentId));

        // Sprawdzenie czy użytkownik jest autorem komentarza lub właścicielem projektu
        boolean isAuthor = comment.getUser().getId().equals(user.getId());
        boolean isProjectOwner = comment.getTask().getProject().getCreatedBy().getId().equals(user.getId());

        if (!isAuthor && !isProjectOwner) {
            throw new UnauthorizedOperationException("Brak uprawnień do usunięcia komentarza");
        }

        // Logowanie audytu
        auditLogService.logActionAsync(userEmail, AuditLogService.ACTION_DELETE, 
                "TaskComment", commentId);

        taskCommentRepository.delete(comment);
    }

    @Transactional(readOnly = true)
    public Long countCommentsForTask(Long taskId) {
        return taskCommentRepository.countByTaskId(taskId);
    }

    private TaskCommentResponse convertToResponse(TaskComment comment) {
        return TaskCommentResponse.builder()
                .id(comment.getId())
                .taskId(comment.getTask().getId())
                .user(userService.convertToUserSummaryResponse(comment.getUser()))
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
