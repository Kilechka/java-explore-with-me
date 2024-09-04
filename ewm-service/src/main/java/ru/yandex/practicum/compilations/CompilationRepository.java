package ru.yandex.practicum.compilations;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {

    @Query("SELECT c FROM Compilation c LEFT JOIN FETCH c.events WHERE c.pinned = :pinned")
    List<Compilation> findAllByPinnedWithEvents(boolean pinned, Pageable pageable);

    @Query("SELECT c FROM Compilation c LEFT JOIN FETCH c.events WHERE c.id = :id")
    Optional<Compilation> findWithEvents(Long id);

    boolean existsByTitle(String title);
}