package ru.yandex.practicum.compilations.dto;

import lombok.*;
import ru.yandex.practicum.events.Event;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompilationDto {
    private Long id;
    private String title;
    @Builder.Default
    private boolean pinned = false;
    List<Event> events;
}