package pl.pbs.zwbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private String message;
    private boolean read;
    private String type;
    private Long relatedEntityId;
    private String relatedEntityType;
    private LocalDate createdAt;
}
