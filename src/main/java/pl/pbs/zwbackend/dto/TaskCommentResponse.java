package pl.pbs.zwbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskCommentResponse {
    private Long id;
    private Long taskId;
    private UserSummaryResponse user;
    private String content;
    private LocalDate createdAt;
    private LocalDateTime updatedAt;
}
