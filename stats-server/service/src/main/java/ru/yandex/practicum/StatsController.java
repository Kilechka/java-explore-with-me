package ru.yandex.practicum;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.model.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class StatsController {

    private final ServiceStat service;

    @PostMapping("/hit")
    @ResponseStatus(code = HttpStatus.CREATED)
    public HitDto saveHit(@RequestBody @Valid HitDto hit) {
        log.info("Post-запрос для сохранения hit  " + hit);
        return service.saveHit(hit);
    }

    @GetMapping("/stats")
    public List<ViewStats> getStatistics(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                    @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                                    @RequestParam(required = false) String[] uris,
                                    @RequestParam(defaultValue = "false") boolean unique) {
        log.info("Get-запрос для получения статистики");
        return service.getStatistics(start, end, uris, unique);
    }
}