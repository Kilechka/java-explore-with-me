package ru.yandex.practicum.requests.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.events.Event;
import ru.yandex.practicum.events.EventRepository;
import ru.yandex.practicum.exception.ConflictDataException;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.requests.Request;
import ru.yandex.practicum.requests.RequestRepository;
import ru.yandex.practicum.requests.Status;
import ru.yandex.practicum.requests.dto.ParticipationRequestDto;
import ru.yandex.practicum.requests.dto.RequestMapper;
import ru.yandex.practicum.users.User;
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
        Request request;
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        validateEvent(event, userId);

        if (event.getParticipantLimit() == 0 || !event.isRequestModeration()) {
            request = Request.builder()
                    .created(LocalDateTime.now())
                    .requester(user)
                    .status(CONFIRMED)
                    .event(event)
                    .build();
            event.addConfirmedRequests();
            eventRepository.save(event);
        } else {
            request = Request.builder()
                    .created(LocalDateTime.now())
                    .requester(user)
                    .status(PENDING)
                    .event(event)
                    .build();
        }
        ParticipationRequestDto participationRequestDto = toParticipationRequestDto(requestRepository.save(request));



        return participationRequestDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getRequest(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }

        return requestRepository.findByRequesterId(userId).stream()
                .map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        Request request =  requestRepository.findByRequesterId(userId)
                        .orElseThrow(() -> new NotFoundException("Запрос не найден"));
        request.setStatus(Status.CANCELED);

        return toParticipationRequestDto(request);
    }

    private void validateEvent(Event event, Long userId) {
        if (event.getState() != PUBLISHED) {
            throw new ConflictDataException("Запрещено участвовать в неопубликованном событии");
        }
        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictDataException("Запрещено участвовать в своём событии");
        }
        if (requestRepository.findByRequesterIdAndEventId(userId, event.getId()).isPresent()) {
            throw new ConflictDataException("Запрещено подавать повторную заявку");
        }
        if (event.getParticipantLimit() > 0) {
            List<Request> requests = requestRepository.findByEventIdAndStatus(event.getId(), CONFIRMED);
            if (event.getParticipantLimit() <= requests.size()) {
                throw new ConflictDataException("Мест не осталось");
            }
        }
    }
}