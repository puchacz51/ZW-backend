package pl.pbs.zwbackend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_user", columnList = "user_id"),
    @Index(name = "idx_audit_entity", columnList = "entity"),
    @Index(name = "idx_audit_entity_id", columnList = "entityId"),
    @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
    @Index(name = "idx_audit_action", columnList = "action")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @NotBlank(message = "Akcja jest wymagana")
    @Column(nullable = false, length = 50)
    private String action;

    @NotBlank(message = "Encja jest wymagana")
    @Column(nullable = false, length = 100)
    private String entity;

    @NotNull(message = "ID encji jest wymagane")
    @Column(nullable = false)
    private Long entityId;

    @Column(length = 1000)
    private String details;

    @Column(length = 45)
    private String ipAddress;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime timestamp;
}
