package ru.yandex.practicum.comments;

import jakarta.persistence.*;
import lombok.*;
import ru.yandex.practicum.events.Event;
import ru.yandex.practicum.users.User;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "changed_on")
    private LocalDateTime changedOn;
    @Column(name = "is_modified")
    private boolean isModified;
    private String text;
    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;
}