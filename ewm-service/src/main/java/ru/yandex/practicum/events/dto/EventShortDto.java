package ru.yandex.practicum.events.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import ru.yandex.practicum.caregories.dto.CategoryDto;
import ru.yandex.practicum.comments.dto.CommentDto;
import ru.yandex.practicum.users.dto.UserShortDto;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventShortDto {
    private Long id;
    private String title;
    private String annotation;
    private CategoryDto category;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    @Builder.Default
    private boolean paid = false;
    private UserShortDto initiator;
    private Long views;
    private int confirmedRequests;
    private List<CommentDto> comments;
}