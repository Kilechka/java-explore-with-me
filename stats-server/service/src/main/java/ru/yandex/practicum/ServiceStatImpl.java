package ru.yandex.practicum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.exceptions.ConflictException;
import ru.yandex.practicum.model.Hit;
import ru.yandex.practicum.model.ViewStats;
import ru.yandex.practicum.repository.HitRepository;
import ru.yandex.practicum.repository.StatRepository;

import java.time.LocalDateTime;
import java.util.List;

import static ru.yandex.practicum.model.HitMapper.toHit;
import static ru.yandex.practicum.model.HitMapper.toHitDto;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ServiceStatImpl implements ServiceStat {

    private final HitRepository repository;
    private final StatRepository statRepository;

    @Override
    public HitDto saveHit(HitDto hitDto) {
        log.info("Сохраняем в сервисе статистику " + hitDto);
        Hit hit = toHit(hitDto);
        repository.save(hit);
        return toHitDto(hit);
    }

    @Override
    public List<ViewStats> getStatistics(LocalDateTime start, LocalDateTime end, String[] uris, boolean unique) {
        if (start.isAfter(end)) {
            throw new ConflictException("Дата начала не может быть позже даты конца");
        }
        return statRepository.getStatistics(start, end, uris, unique);
    }
}