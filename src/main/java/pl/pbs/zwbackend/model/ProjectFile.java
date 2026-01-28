package pl.pbs.zwbackend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "project_files", indexes = {
    @Index(name = "idx_project_file_project", columnList = "project_id"),
    @Index(name = "idx_project_file_uploaded_by", columnList = "uploaded_by"),
    @Index(name = "idx_project_file_upload_date", columnList = "uploadDate"),
    @Index(name = "idx_project_file_stored_name", columnList = "storedFileName", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Oryginalna nazwa pliku jest wymagana")
    @Size(max = 255, message = "Nazwa pliku nie może przekraczać 255 znaków")
    @Column(nullable = false, length = 255)
    private String originalFileName;

    @NotBlank(message = "Nazwa przechowywanego pliku jest wymagana")
    @Column(nullable = false, length = 500, unique = true)
    private String storedFileName;

    @NotBlank(message = "Typ zawartości jest wymagany")
    @Column(nullable = false)
    private String contentType;

    @NotNull(message = "Rozmiar pliku jest wymagany")
    @Positive(message = "Rozmiar pliku musi być dodatni")
    @Column(nullable = false)
    private Long fileSize;

    @Size(max = 1000, message = "Opis nie może przekraczać 1000 znaków")
    @Column(length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime uploadDate;
}
