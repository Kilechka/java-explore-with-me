package ru.yandex.practicum.events.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.yandex.practicum.requests.Status;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventRequestStatusUpdateRequest {
    @NotNull
    private Status status;
    @NotNull
    private List<Long> requestIds;
}