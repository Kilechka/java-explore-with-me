package ru.yandex.practicum.model;

import ru.yandex.practicum.HitDto;

public class HitMapper {

    private HitMapper() {
    }

    public static HitDto toHitDto(Hit hit) {
        return HitDto.builder()
                .app(hit.getApp())
                .uri(hit.getUri())
                .timestamp(hit.getTimestamp())
                .ip(hit.getIp())
                .build();
    }

    public static Hit toHit(HitDto hitDto) {
        return Hit.builder()
                .app(hitDto.getApp())
                .uri(hitDto.getUri())
                .timestamp(hitDto.getTimestamp())
                .ip(hitDto.getIp())
                .build();
    }
}