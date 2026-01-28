package pl.pbs.zwbackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.pbs.zwbackend.dto.TaskCommentRequest;
import pl.pbs.zwbackend.dto.TaskCommentResponse;
import pl.pbs.zwbackend.service.TaskCommentService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/task-comments")
@RequiredArgsConstructor
@Tag(name = "Task Comments", description = "API do zarządzania komentarzami zadań")
public class TaskCommentController {

    private final TaskCommentService taskCommentService;

    @PostMapping
    @Operation(summary = "Dodaj komentarz do zadania")
    public ResponseEntity<TaskCommentResponse> createComment(
            @Valid @RequestBody TaskCommentRequest request,
            Authentication authentication
    ) {
        TaskCommentResponse response = taskCommentService.createComment(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/task/{taskId}")
    @Operation(summary = "Pobierz komentarze dla zadania")
    public ResponseEntity<List<TaskCommentResponse>> getCommentsForTask(@PathVariable Long taskId) {
        List<TaskCommentResponse> comments = taskCommentService.getCommentsForTask(taskId);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/{commentId}")
    @Operation(summary = "Pobierz komentarz po ID")
    public ResponseEntity<TaskCommentResponse> getCommentById(@PathVariable Long commentId) {
        TaskCommentResponse comment = taskCommentService.getCommentById(commentId);
        return ResponseEntity.ok(comment);
    }

    @PutMapping("/{commentId}")
    @Operation(summary = "Zaktualizuj komentarz")
    public ResponseEntity<TaskCommentResponse> updateComment(
            @PathVariable Long commentId,
            @RequestBody Map<String, String> request,
            Authentication authentication
    ) {
        String newContent = request.get("content");
        if (newContent == null || newContent.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        TaskCommentResponse response = taskCommentService.updateComment(
                commentId, newContent, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "Usuń komentarz")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            Authentication authentication
    ) {
        taskCommentService.deleteComment(commentId, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/task/{taskId}/count")
    @Operation(summary = "Pobierz liczbę komentarzy dla zadania")
    public ResponseEntity<Map<String, Long>> getCommentCount(@PathVariable Long taskId) {
        Long count = taskCommentService.countCommentsForTask(taskId);
        return ResponseEntity.ok(Map.of("count", count));
    }
}
