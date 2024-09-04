package ru.yandex.practicum.requests;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {


    Optional<Request> findByRequesterIdAndEventId(Long requesterId, Long eventId);
    Optional<Request> findByRequesterId(Long requesterId);

    List<Request> findByEventId(Long eventId);

    List<Request> findAllByIdInAndEventId(List<Long> ids, Long eventId);
    List<Request> findByEventIdAndStatus(Long eventId, Status status);
}