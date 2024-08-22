package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.model.Hit;

public interface HitRepository extends JpaRepository<Hit, Integer> {

}