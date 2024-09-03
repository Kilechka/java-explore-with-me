package ru.yandex.practicum.events.service;

import ru.yandex.practicum.events.dto.*;
import ru.yandex.practicum.requests.dto.ParticipationRequestDto;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    EventFullDto createEvent(NewEventDto newEventDto, Long userId);

    List<EventShortDto> getEventsByUser(Long userId, int from, int size);

    EventFullDto getEventByUser(Long userId, Long eventId);

    EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId);

    EventFullDto updateStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest eventRequestStatus);

    EventFullDto getEvent(Long id, HttpServletRequest request);

    List<EventShortDto> getEvents(String text, List<Long> categories,Boolean paid,
                                         LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                         Boolean onlyAvailable, String sort, int from,
                                         int size, HttpServletRequest request);

    List<EventFullDto> getEventsForAdmin(List<Long> users, List<String> states,
                                         List<Long> categories, LocalDateTime rangeStart,
                                         LocalDateTime rangeEnd, int from, int size);

    EventFullDto updateEventForAdmin(Long eventId, UpdateEventAdminRequest request);
}