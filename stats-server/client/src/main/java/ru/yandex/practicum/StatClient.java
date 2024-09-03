package ru.yandex.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@Slf4j
public class StatClient extends BaseClient {

    @Autowired
    public StatClient(@Value("http://stats-server:9090") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public void saveHit(HitDto hit) {
        log.info("Сохраняем в клиенте статистику");
        post("/hit", hit);
    }

    public ResponseEntity<Object> getStatistics(LocalDateTime start, LocalDateTime end, String[] uris, Boolean unique) {
        log.info("Получаем в клиенте статистику");
        Map<String, Object> parameters = Map.of(
                "start", start.toString(),
                "end", end.toString(),
                "uris", String.join(",", uris),
                "unique", unique.toString()
        );

        return get("/stats?start={start}&end={end}&uris={uris}&unique={unique}", parameters);
    }
}