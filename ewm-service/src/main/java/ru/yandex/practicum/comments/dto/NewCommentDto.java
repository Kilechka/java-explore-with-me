package ru.yandex.practicum.comments.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewCommentDto {
    @NotNull
    private long eventId;
    @NotBlank
    @Size(min = 1, max = 1000)
    private String text;
}