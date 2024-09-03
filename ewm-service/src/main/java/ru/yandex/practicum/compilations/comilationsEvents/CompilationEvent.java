package ru.yandex.practicum.compilations.comilationsEvents;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@IdClass(CompilationEventId.class)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "compilation_events")
public class CompilationEvent {
    @Id
    @Column(name = "compilation_id", nullable = false)
    private Long compilationId;
    @Id
    @Column(name = "event_Id", nullable = false)
    private Long eventId;
}