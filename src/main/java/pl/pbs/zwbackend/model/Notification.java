package pl.pbs.zwbackend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;

@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notification_user", columnList = "user_id"),
    @Index(name = "idx_notification_read", columnList = "read"),
    @Index(name = "idx_notification_created_at", columnList = "createdAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank(message = "Treść powiadomienia jest wymagana")
    @Size(min = 1, max = 500, message = "Powiadomienie musi mieć od 1 do 500 znaków")
    @Column(nullable = false, length = 500)
    private String message;

    @Column(nullable = false)
    @Builder.Default
    private boolean read = false;

    @Column(length = 50)
    private String type;

    @Column
    private Long relatedEntityId;

    @Column(length = 100)
    private String relatedEntityType;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDate createdAt;
}
