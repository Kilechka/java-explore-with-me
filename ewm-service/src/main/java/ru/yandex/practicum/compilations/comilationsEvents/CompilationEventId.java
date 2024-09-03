package ru.yandex.practicum.compilations.comilationsEvents;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class CompilationEventId implements Serializable {
    private Long compilationId;
    private Long eventId;
}