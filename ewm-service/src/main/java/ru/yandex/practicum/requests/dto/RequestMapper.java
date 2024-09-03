package ru.yandex.practicum.requests.dto;

import ru.yandex.practicum.requests.Request;

public class RequestMapper {

    private RequestMapper() {
    }

    public static ParticipationRequestDto toParticipationRequestDto(Request request) {
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .requester(request.getRequester())
                .event(request.getEvent())
                .status(request.getStatus())
                .created(request.getCreated())
                .build();
    }
}