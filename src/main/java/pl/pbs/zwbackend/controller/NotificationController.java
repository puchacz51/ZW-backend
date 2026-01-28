package pl.pbs.zwbackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.pbs.zwbackend.dto.NotificationResponse;
import pl.pbs.zwbackend.model.Notification;
import pl.pbs.zwbackend.model.User;
import pl.pbs.zwbackend.repository.UserRepository;
import pl.pbs.zwbackend.service.NotificationService;
import pl.pbs.zwbackend.exception.ResourceNotFoundException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "API do zarządzania powiadomieniami")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Pobierz wszystkie powiadomienia zalogowanego użytkownika")
    public ResponseEntity<Page<NotificationResponse>> getMyNotifications(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        User user = getCurrentUser(authentication);
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationService.getNotificationsForUser(user.getId(), pageable);
        Page<NotificationResponse> response = notifications.map(this::convertToResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/unread")
    @Operation(summary = "Pobierz nieprzeczytane powiadomienia")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications(Authentication authentication) {
        User user = getCurrentUser(authentication);
        List<Notification> notifications = notificationService.getUnreadNotificationsForUser(user.getId());
        List<NotificationResponse> response = notifications.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/count")
    @Operation(summary = "Pobierz liczbę nieprzeczytanych powiadomień")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication authentication) {
        User user = getCurrentUser(authentication);
        Long count = notificationService.countUnreadNotifications(user.getId());
        Map<String, Long> response = new HashMap<>();
        response.put("unreadCount", count);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{notificationId}/read")
    @Operation(summary = "Oznacz powiadomienie jako przeczytane")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Long notificationId,
            Authentication authentication
    ) {
        Notification notification = notificationService.markAsRead(notificationId);
        return ResponseEntity.ok(convertToResponse(notification));
    }

    @PutMapping("/read-all")
    @Operation(summary = "Oznacz wszystkie powiadomienia jako przeczytane")
    public ResponseEntity<Map<String, Integer>> markAllAsRead(Authentication authentication) {
        User user = getCurrentUser(authentication);
        int updatedCount = notificationService.markAllAsRead(user.getId());
        Map<String, Integer> response = new HashMap<>();
        response.put("updatedCount", updatedCount);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{notificationId}")
    @Operation(summary = "Usuń powiadomienie")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long notificationId,
            Authentication authentication
    ) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/all")
    @Operation(summary = "Usuń wszystkie powiadomienia")
    public ResponseEntity<Void> deleteAllNotifications(Authentication authentication) {
        User user = getCurrentUser(authentication);
        notificationService.deleteAllNotificationsForUser(user.getId());
        return ResponseEntity.noContent().build();
    }

    private User getCurrentUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", authentication.getName()));
    }

    private NotificationResponse convertToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .read(notification.isRead())
                .type(notification.getType())
                .relatedEntityId(notification.getRelatedEntityId())
                .relatedEntityType(notification.getRelatedEntityType())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
