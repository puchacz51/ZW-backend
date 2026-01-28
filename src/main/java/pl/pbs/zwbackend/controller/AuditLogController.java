package pl.pbs.zwbackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.pbs.zwbackend.dto.AuditLogResponse;
import pl.pbs.zwbackend.dto.UserSummaryResponse;
import pl.pbs.zwbackend.model.AuditLog;
import pl.pbs.zwbackend.model.User;
import pl.pbs.zwbackend.repository.UserRepository;
import pl.pbs.zwbackend.service.AuditLogService;
import pl.pbs.zwbackend.service.UserService;
import pl.pbs.zwbackend.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Tag(name = "Audit Log", description = "API do zarządzania logami audytu")
public class AuditLogController {

    private final AuditLogService auditLogService;
    private final UserRepository userRepository;
    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Pobierz wszystkie logi audytu", description = "Tylko dla administratorów")
    public ResponseEntity<Page<AuditLogResponse>> getAllLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> logs = auditLogService.getAllLogs(pageable);
        Page<AuditLogResponse> response = logs.map(this::convertToResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isCurrentUser(#userId)")
    @Operation(summary = "Pobierz logi audytu dla użytkownika")
    public ResponseEntity<Page<AuditLogResponse>> getLogsForUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> logs = auditLogService.getLogsForUser(userId, pageable);
        Page<AuditLogResponse> response = logs.map(this::convertToResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    @Operation(summary = "Pobierz własne logi audytu")
    public ResponseEntity<List<AuditLogResponse>> getMyLogs(
            Authentication authentication,
            @RequestParam(defaultValue = "24") int hoursBack
    ) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", authentication.getName()));
        
        List<AuditLog> logs = auditLogService.getRecentLogsForUser(user.getId(), hoursBack);
        List<AuditLogResponse> response = logs.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/entity/{entity}/{entityId}")
    @Operation(summary = "Pobierz logi audytu dla konkretnej encji")
    public ResponseEntity<List<AuditLogResponse>> getLogsForEntity(
            @PathVariable String entity,
            @PathVariable Long entityId
    ) {
        List<AuditLog> logs = auditLogService.getLogsForEntity(entity, entityId);
        List<AuditLogResponse> response = logs.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Pobierz logi audytu z zakresu dat")
    public ResponseEntity<List<AuditLogResponse>> getLogsByDateRange(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate
    ) {
        List<AuditLog> logs = auditLogService.getLogsBetweenDates(startDate, endDate);
        List<AuditLogResponse> response = logs.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats/{entity}/{action}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Pobierz statystyki akcji dla encji")
    public ResponseEntity<Long> getActionStats(
            @PathVariable String entity,
            @PathVariable String action
    ) {
        Long count = auditLogService.countActionsForEntity(entity, action);
        return ResponseEntity.ok(count);
    }

    private AuditLogResponse convertToResponse(AuditLog log) {
        UserSummaryResponse userSummary = userService.convertToUserSummaryResponse(log.getUser());
        
        return AuditLogResponse.builder()
                .id(log.getId())
                .user(userSummary)
                .action(log.getAction())
                .entity(log.getEntity())
                .entityId(log.getEntityId())
                .details(log.getDetails())
                .ipAddress(log.getIpAddress())
                .timestamp(log.getTimestamp())
                .build();
    }
}
