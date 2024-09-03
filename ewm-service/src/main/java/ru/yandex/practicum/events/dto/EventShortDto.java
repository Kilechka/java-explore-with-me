package ru.yandex.practicum.events.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.caregories.dto.CategoryDto;
import ru.yandex.practicum.users.dto.UserShortDto;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventShortDto {
    private Long id;
    private String title;
    private String annotation;
    private CategoryDto category;
    private LocalDateTime eventDate;
    @Builder.Default
    private boolean paid = false;
    private UserShortDto initiator;
    private Long views;
    private int confirmedRequests;
}