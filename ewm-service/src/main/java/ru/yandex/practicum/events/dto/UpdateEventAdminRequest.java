package ru.yandex.practicum.events.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.events.locations.Location;
import ru.yandex.practicum.events.StateActionAdmin;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class UpdateEventAdminRequest {
    private String title;
    private String annotation;
    private String description;
    private Integer category;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    private Location location;
    @Builder.Default
    private boolean paid = false;
    private Integer participantLimit;
    @Builder.Default
    private boolean requestModeration = true;
    private StateActionAdmin stateAction;
}