package ru.yandex.practicum.events;

import jakarta.persistence.*;
import lombok.*;
import ru.yandex.practicum.caregories.Category;
import ru.yandex.practicum.events.locations.Location;
import ru.yandex.practicum.users.User;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;
    @Column(name = "annotation", nullable = false)
    private String annotation;
    @Column(name = "description", nullable = false)
    private String description;
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;
    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;
    @ManyToOne
    @JoinColumn(name = "initiator_id", nullable = false)
    private User initiator;
    private boolean paid;
    @Column(name = "participant_limit", nullable = false)
    private int participantLimit;
    @Column(name = "request_moderation")
    private boolean requestModeration;
    @Column(name = "created_on", nullable = false)
    private LocalDateTime createdOn;
    @Column(name = "published_on", nullable = false)
    private LocalDateTime publishedOn;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private State state;
    private Long views;
    @Column(name = "confirmed_requests")
    private int confirmedRequests;

    public void addConfirmedRequests() {
        confirmedRequests++;
    }
}