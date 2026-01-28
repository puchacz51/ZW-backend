package pl.pbs.zwbackend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.pbs.zwbackend.model.AuditLog;
import pl.pbs.zwbackend.model.User;
import pl.pbs.zwbackend.repository.AuditLogRepository;
import pl.pbs.zwbackend.repository.UserRepository;
import pl.pbs.zwbackend.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    /**
     * Loguje akcję użytkownika asynchronicznie
     */
    @Async
    @Transactional
    public void logActionAsync(String userEmail, String action, String entity, Long entityId) {
        logAction(userEmail, action, entity, entityId);
    }

    /**
     * Loguje akcję użytkownika synchronicznie
     */
    @Transactional
    public AuditLog logAction(String userEmail, String action, String entity, Long entityId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));
        
        return logAction(user, action, entity, entityId);
    }

    /**
     * Loguje akcję użytkownika z obiektem User
     */
    @Transactional
    public AuditLog logAction(User user, String action, String entity, Long entityId) {
        AuditLog auditLog = AuditLog.builder()
                .user(user)
                .action(action)
                .entity(entity)
                .entityId(entityId)
                .build();
        
        return auditLogRepository.save(auditLog);
    }

    /**
     * Pobiera logi dla konkretnej encji
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getLogsForEntity(String entity, Long entityId) {
        return auditLogRepository.findByEntityAndEntityId(entity, entityId);
    }

    /**
     * Pobiera logi użytkownika
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getLogsForUser(Long userId) {
        return auditLogRepository.findByUserId(userId);
    }

    /**
     * Pobiera logi użytkownika z paginacją
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getLogsForUser(Long userId, Pageable pageable) {
        return auditLogRepository.findByUserIdOrderByTimestampDesc(userId, pageable);
    }

    /**
     * Pobiera wszystkie logi z paginacją
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAllLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByTimestampDesc(pageable);
    }

    /**
     * Pobiera logi z zakresu dat
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getLogsBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.findByTimestampBetween(startDate, endDate);
    }

    /**
     * Pobiera ostatnie logi użytkownika
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getRecentLogsForUser(Long userId, int hoursBack) {
        LocalDateTime since = LocalDateTime.now().minusHours(hoursBack);
        return auditLogRepository.findRecentByUserId(userId, since);
    }

    /**
     * Zlicza akcje dla typu encji
     */
    @Transactional(readOnly = true)
    public Long countActionsForEntity(String entity, String action) {
        return auditLogRepository.countByEntityAndAction(entity, action);
    }

    // Stałe dla typów akcji
    public static final String ACTION_CREATE = "CREATE";
    public static final String ACTION_UPDATE = "UPDATE";
    public static final String ACTION_DELETE = "DELETE";
    public static final String ACTION_VIEW = "VIEW";
    public static final String ACTION_LOGIN = "LOGIN";
    public static final String ACTION_LOGOUT = "LOGOUT";
    public static final String ACTION_ASSIGN = "ASSIGN";
    public static final String ACTION_UNASSIGN = "UNASSIGN";

    // Stałe dla typów encji
    public static final String ENTITY_PROJECT = "Project";
    public static final String ENTITY_TASK = "Task";
    public static final String ENTITY_USER = "User";
    public static final String ENTITY_COMMENT = "Comment";
    public static final String ENTITY_FILE = "File";
}
