package pl.pbs.zwbackend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages", indexes = {
    @Index(name = "idx_message_sender", columnList = "sender_id"),
    @Index(name = "idx_message_timestamp", columnList = "timestamp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @NotBlank(message = "Treść wiadomości jest wymagana")
    @Size(min = 1, max = 1000, message = "Wiadomość musi mieć od 1 do 1000 znaków")
    @Column(nullable = false, length = 1000)
    private String content;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime timestamp;
}
