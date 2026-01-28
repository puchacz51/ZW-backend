package pl.pbs.zwbackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import pl.pbs.zwbackend.model.enums.Role;

import java.time.LocalDateTime;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email", unique = true),
    @Index(name = "idx_user_role", columnList = "role"),
    @Index(name = "idx_user_created_at", columnList = "createdAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Imię jest wymagane")
    @Size(min = 1, max = 50, message = "Imię musi mieć od 1 do 50 znaków")
    @Column(nullable = false, length = 50)
    private String firstName;

    @NotBlank(message = "Nazwisko jest wymagane")
    @Size(min = 1, max = 50, message = "Nazwisko musi mieć od 1 do 50 znaków")
    @Column(nullable = false, length = 50)
    private String lastName;

    @NotBlank(message = "Email jest wymagany")
    @Email(message = "Nieprawidłowy format email")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 255)
    private String avatarFileName;

    @Column(length = 50)
    private String avatarContentType;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public void setPassword(String rawPassword) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        this.password = encoder.encode(rawPassword);
    }

    // Metoda pomocnicza do pełnego imienia
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
