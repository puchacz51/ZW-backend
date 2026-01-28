package pl.pbs.zwbackend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "task_comments", indexes = {
    @Index(name = "idx_task_comment_task", columnList = "task_id"),
    @Index(name = "idx_task_comment_user", columnList = "user_id"),
    @Index(name = "idx_task_comment_created_at", columnList = "createdAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank(message = "Treść komentarza jest wymagana")
    @Size(min = 1, max = 500, message = "Komentarz musi mieć od 1 do 500 znaków")
    @Column(nullable = false, length = 500)
    private String content;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDate createdAt;

    @Column
    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
