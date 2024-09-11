package ru.yandex.practicum.requests.service;

import ru.yandex.practicum.requests.dto.ParticipationRequestDto;

import java.util.List;

public interface RequestService {

    ParticipationRequestDto createRequest(Long userId, Long eventId);

    List<ParticipationRequestDto> getRequest(Long userId);

    ParticipationRequestDto cancelRequest(Long userId, Long eventId);
}