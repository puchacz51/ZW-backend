package pl.pbs.zwbackend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import pl.pbs.zwbackend.model.enums.ProjectStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "projects", indexes = {
    @Index(name = "idx_project_created_by", columnList = "created_by"),
    @Index(name = "idx_project_status", columnList = "status"),
    @Index(name = "idx_project_created_at", columnList = "createdAt"),
    @Index(name = "idx_project_name", columnList = "name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nazwa projektu jest wymagana")
    @Size(min = 1, max = 255, message = "Nazwa projektu musi mieć od 1 do 255 znaków")
    @Column(nullable = false, length = 255)
    private String name;

    @Size(max = 1000, message = "Opis nie może przekraczać 1000 znaków")
    @Column(length = 1000)
    private String description;

    private LocalDate startDate;
    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDate createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ProjectStatus status = ProjectStatus.NOT_STARTED;

    // Relacje z kaskadowym usuwaniem
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Task> tasks = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProjectComment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProjectFile> files = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProjectUser> projectUsers = new ArrayList<>();

    // Walidacja dat
    @PrePersist
    @PreUpdate
    private void validateDates() {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Data zakończenia nie może być wcześniejsza niż data rozpoczęcia");
        }
    }
}
