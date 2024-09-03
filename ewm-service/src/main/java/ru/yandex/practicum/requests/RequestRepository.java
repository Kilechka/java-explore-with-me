package ru.yandex.practicum.requests;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {

    Optional<Request> findByRequesterAndEvent(Long requester, Long eventId);

    List<Request> findByRequester(Long requester);

    List<Request> findByEvent(Long eventId);

    List<Request> findAllByIdInAndEvent(List<Long> ids, Long eventId);
}