package ru.yandex.practicum;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.time.LocalDateTime;
import java.util.Map;

public class StatClient extends BaseClient {

    @Autowired
    public StatClient(@Value("${server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> saveHit(HitDto hit) {
        return post("hit", hit);
    }

    public ResponseEntity<Object> getStatistics(LocalDateTime start, LocalDateTime end, String[] uris, Boolean unique) {
        Map<String, Object> parameters = Map.of(
                "start", start.toString(),
                "end", end.toString(),
                "uris", String.join(",", uris),
                "unique", unique.toString()
        );

        return get("/stats?start={start}&end={end}&uris={uris}&unique={unique}", parameters);
    }
}