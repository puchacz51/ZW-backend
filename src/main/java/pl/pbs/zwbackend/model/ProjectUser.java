package pl.pbs.zwbackend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import pl.pbs.zwbackend.model.enums.ProjectRole;

import java.time.LocalDateTime;

@Entity
@Table(name = "project_user", indexes = {
    @Index(name = "idx_project_user_project", columnList = "project_id"),
    @Index(name = "idx_project_user_user", columnList = "user_id"),
    @Index(name = "idx_project_user_role", columnList = "role")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(ProjectUserId.class)
public class ProjectUser {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProjectRole role;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime assignedAt;
}
