package pl.pbs.zwbackend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.pbs.zwbackend.model.Notification;
import pl.pbs.zwbackend.model.User;
import pl.pbs.zwbackend.repository.NotificationRepository;
import pl.pbs.zwbackend.repository.UserRepository;
import pl.pbs.zwbackend.exception.ResourceNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * Tworzy powiadomienie dla użytkownika
     */
    @Transactional
    public Notification createNotification(Long userId, String message) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        Notification notification = Notification.builder()
                .user(user)
                .message(message)
                .read(false)
                .build();
        
        return notificationRepository.save(notification);
    }

    /**
     * Tworzy powiadomienie dla użytkownika asynchronicznie
     */
    @Async
    @Transactional
    public void createNotificationAsync(Long userId, String message) {
        createNotification(userId, message);
    }

    /**
     * Pobiera wszystkie powiadomienia użytkownika
     */
    @Transactional(readOnly = true)
    public List<Notification> getNotificationsForUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Pobiera powiadomienia użytkownika z paginacją
     */
    @Transactional(readOnly = true)
    public Page<Notification> getNotificationsForUser(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * Pobiera nieprzeczytane powiadomienia użytkownika
     */
    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotificationsForUser(Long userId) {
        return notificationRepository.findByUserIdAndReadFalse(userId);
    }

    /**
     * Zlicza nieprzeczytane powiadomienia
     */
    @Transactional(readOnly = true)
    public Long countUnreadNotifications(Long userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    /**
     * Oznacza powiadomienie jako przeczytane
     */
    @Transactional
    public Notification markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));
        
        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    /**
     * Oznacza wszystkie powiadomienia użytkownika jako przeczytane
     */
    @Transactional
    public int markAllAsRead(Long userId) {
        return notificationRepository.markAllAsReadByUserId(userId);
    }

    /**
     * Usuwa powiadomienie
     */
    @Transactional
    public void deleteNotification(Long notificationId) {
        if (!notificationRepository.existsById(notificationId)) {
            throw new ResourceNotFoundException("Notification", "id", notificationId);
        }
        notificationRepository.deleteById(notificationId);
    }

    /**
     * Usuwa wszystkie powiadomienia użytkownika
     */
    @Transactional
    public void deleteAllNotificationsForUser(Long userId) {
        notificationRepository.deleteAllByUserId(userId);
    }

    // Metody pomocnicze do tworzenia typowych powiadomień

    public void notifyTaskAssignment(Long userId, String taskName, String projectName) {
        String message = String.format("Zostałeś przypisany do zadania '%s' w projekcie '%s'", taskName, projectName);
        createNotificationAsync(userId, message);
    }

    public void notifyProjectInvitation(Long userId, String projectName) {
        String message = String.format("Zostałeś dodany do projektu '%s'", projectName);
        createNotificationAsync(userId, message);
    }

    public void notifyTaskStatusChange(Long userId, String taskName, String newStatus) {
        String message = String.format("Status zadania '%s' został zmieniony na '%s'", taskName, newStatus);
        createNotificationAsync(userId, message);
    }

    public void notifyNewComment(Long userId, String authorName, String context) {
        String message = String.format("%s dodał komentarz: %s", authorName, context);
        createNotificationAsync(userId, message);
    }

    public void notifyProjectDeadlineApproaching(Long userId, String projectName, int daysLeft) {
        String message = String.format("Termin projektu '%s' upływa za %d dni", projectName, daysLeft);
        createNotificationAsync(userId, message);
    }
}
