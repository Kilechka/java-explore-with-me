package ru.yandex.practicum.events.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.yandex.practicum.events.locations.Location;
import ru.yandex.practicum.events.StateActionUser;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEventUserRequest {
    @Size(min = 3, max = 120)
    private String title;
    @Size(min = 20, max = 2000)
    private String annotation;
    @Size(min = 20, max = 7000)
    private String description;
    private Integer category;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    private Location location;
    @Builder.Default
    private boolean paid = false;
    @PositiveOrZero
    @Builder.Default
    @PositiveOrZero
    private int participantLimit = 0;
    @Builder.Default
    private boolean requestModeration = true;
    private StateActionUser stateAction;
}