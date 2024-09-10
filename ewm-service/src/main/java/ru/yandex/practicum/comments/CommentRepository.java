package ru.yandex.practicum.comments;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.events.Event;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Optional<Comment> findByCreatorIdAndId(Long CreatorId, Long id);

    List<Comment> findAllByEventId(Long EventId, Pageable pageable);
}