package ru.yandex.practicum.compilations.comilationsEvents;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CompilationEventRepository extends JpaRepository<CompilationEvent, CompilationEventId> {
    void deleteAllByCompilationId(Long compilationId);
}