package ru.yandex.practicum.events.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.caregories.dto.CategoryDto;
import ru.yandex.practicum.events.locations.Location;
import ru.yandex.practicum.events.State;
import ru.yandex.practicum.users.dto.UserShortDto;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventFullDto {
    private Long id;
    private String title;
    private String annotation;
    private String description;
    private CategoryDto category;
    private LocalDateTime eventDate;
    private Location location;
    @Builder.Default
    private boolean paid = false;
    @Builder.Default
    private int participantLimit = 0;
    @Builder.Default
    private boolean requestModeration = true;
    private UserShortDto initiator;
    private LocalDateTime createdOn;
    private LocalDateTime publishedOn;
    private State state;
    private Long views;
    private int confirmedRequests;
}