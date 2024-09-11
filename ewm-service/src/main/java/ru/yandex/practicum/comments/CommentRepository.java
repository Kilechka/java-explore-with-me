package ru.yandex.practicum.comments;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Optional<Comment> findByCreatorIdAndId(Long creatorId, Long id);

    List<Comment> findAllByEventId(Long eventId, Pageable pageable);
}