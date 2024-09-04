package ru.yandex.practicum.requests;

import jakarta.persistence.*;
import lombok.*;
import ru.yandex.practicum.events.Event;
import ru.yandex.practicum.users.User;

import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "requests")
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private LocalDateTime created;
    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;
    @ManyToOne
    @JoinColumn(name = "requester_id")
    private User requester;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;
}