package ru.yandex.practicum;

import ru.yandex.practicum.model.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface ServiceStat {
    HitDto saveHit(HitDto hit);
    List<ViewStats> getStatistics(LocalDateTime start, LocalDateTime end, String[] uris, boolean unique);
}