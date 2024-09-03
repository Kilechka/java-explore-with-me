package ru.yandex.practicum.requests.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.events.Event;
import ru.yandex.practicum.events.EventRepository;
import ru.yandex.practicum.exception.ConflictException;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.requests.Request;
import ru.yandex.practicum.requests.RequestRepository;
import ru.yandex.practicum.requests.dto.ParticipationRequestDto;
import ru.yandex.practicum.requests.dto.RequestMapper;
import ru.yandex.practicum.users.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static ru.yandex.practicum.events.State.PUBLISHED;
import static ru.yandex.practicum.requests.Status.*;
import static ru.yandex.practicum.requests.dto.RequestMapper.toParticipationRequestDto;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }

        if (event.getParticipantLimit() == event.getConfirmedRequests() && event.getParticipantLimit() != 0) {
            throw new ConflictException("Достигнут лимит участников у события");
        }
        if (event.getState() != PUBLISHED) {
            throw new ConflictException("Запрещено участвовать в неопубликованном событии");
        }
        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Запрещено участвовать в своём событии");
        }
        if (requestRepository.findByRequesterAndEvent(userId, eventId).isPresent()) {
            throw new ConflictException("Запрещено подавать повторную заявку");
        }

        Request request;

        if (!event.isRequestModeration()) {
            request = Request.builder()
                    .created(LocalDateTime.now())
                    .requester(userId)
                    .status(CONFIRMED)
                    .event(eventId)
                    .build();
        } else {
            request = Request.builder()
                    .created(LocalDateTime.now())
                    .requester(userId)
                    .status(PENDING)
                    .event(eventId)
                    .build();
        }

        event.addConfirmedRequests();
        eventRepository.save(event);

        return toParticipationRequestDto(requestRepository.save(request));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getRequest(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }

        return requestRepository.findByRequester(userId).stream()
                .map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        Request request = requestRepository.findByRequesterAndEvent(userId, requestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден или недоступен"));
        request.setStatus(REJECTED);

        return toParticipationRequestDto(request);
    }
}