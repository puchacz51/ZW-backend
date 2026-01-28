package pl.pbs.zwbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskCommentRequest {
    
    @NotNull(message = "ID zadania jest wymagane")
    private Long taskId;
    
    @NotBlank(message = "Treść komentarza jest wymagana")
    @Size(min = 1, max = 500, message = "Komentarz musi mieć od 1 do 500 znaków")
    private String content;
}
