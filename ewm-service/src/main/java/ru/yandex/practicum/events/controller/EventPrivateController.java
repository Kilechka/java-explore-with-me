package ru.yandex.practicum.events.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.events.dto.*;
import ru.yandex.practicum.events.service.EventService;
import ru.yandex.practicum.requests.dto.ParticipationRequestDto;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/events")
public class EventPrivateController {

    private final EventService eventService;

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public EventFullDto createEvent(@RequestBody @Valid NewEventDto newEventDto,
                                    @PathVariable Long userId) {
        log.info("Получен запрос на создание события");
        return eventService.createEvent(newEventDto, userId);
    }

    @GetMapping
    public List<EventShortDto> getEventsByUser(@PathVariable Long userId,
                                               @RequestParam(value = "from", defaultValue = "0") @PositiveOrZero int from,
                                               @RequestParam(value = "size", defaultValue = "10") @Positive int size) {
        log.info("Получен запрос на получение событий");
        return eventService.getEventsByUser(userId, from, size);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventByUser(@PathVariable Long userId,
                                       @PathVariable Long eventId) {
        log.info("Получен запрос на получение события");
        return eventService.getEventByUser(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(@PathVariable Long userId,
                                    @PathVariable Long eventId,
                                    @RequestBody UpdateEventUserRequest updateEventUserRequest) {
        log.info("Получен запрос на обновление события");
        return eventService.updateEvent(userId, eventId, updateEventUserRequest);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getEventRequests(@PathVariable Long userId,
                                                    @PathVariable Long eventId) {
        log.info("Получен запрос на получение запросов события");
        return eventService.getEventRequests(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventFullDto updateStatus(@PathVariable Long userId,
                                     @PathVariable Long eventId,
                                     @RequestBody EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        log.info("Получен запрос на обновление статуса");
        return eventService.updateStatus(userId, eventId, eventRequestStatusUpdateRequest);
    }
}