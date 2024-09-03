package ru.yandex.practicum.events.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.HitDto;
import ru.yandex.practicum.StatClient;
import ru.yandex.practicum.ViewStats;
import ru.yandex.practicum.caregories.Category;
import ru.yandex.practicum.caregories.CategoryRepository;
import ru.yandex.practicum.events.Event;
import ru.yandex.practicum.events.EventRepository;
import ru.yandex.practicum.events.State;
import ru.yandex.practicum.events.dto.*;
import ru.yandex.practicum.events.locations.Location;
import ru.yandex.practicum.events.locations.LocationRepository;
import ru.yandex.practicum.exception.ConflictException;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.requests.Request;
import ru.yandex.practicum.requests.RequestRepository;
import ru.yandex.practicum.requests.dto.ParticipationRequestDto;
import ru.yandex.practicum.requests.dto.RequestMapper;
import ru.yandex.practicum.users.User;
import ru.yandex.practicum.users.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static ru.yandex.practicum.events.State.CANCELED;
import static ru.yandex.practicum.events.State.PUBLISHED;
import static ru.yandex.practicum.events.StateActionAdmin.PUBLISH_EVENT;
import static ru.yandex.practicum.events.StateActionAdmin.REJECT_EVENT;
import static ru.yandex.practicum.events.StateActionUser.CANCEL_REVIEW;
import static ru.yandex.practicum.events.dto.EventMapper.toEvent;
import static ru.yandex.practicum.events.dto.EventMapper.toEventFullDto;
import static ru.yandex.practicum.requests.Status.PENDING;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final StatClient statClient;
    private final ObjectMapper objectMapper;
    private final LocationRepository locationRepository;

    @Override
    public EventFullDto createEvent(NewEventDto newEventDto, Long userId) {
        log.info("В сервисе создаем событие");

        LocalDateTime nowPlusTwoHours = LocalDateTime.now().plusHours(2);
        if (newEventDto.getEventDate().isBefore(nowPlusTwoHours)) {
            throw new ConflictException("Дата и время события должны быть не раньше, " +
                    "чем через два часа от текущего момента.");
        }

        Event event = toEvent(newEventDto);

        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Категория не найдена"));
        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        event.setInitiator(initiator);
        event.setCategory(category);
        event.setCreatedOn(LocalDateTime.now());
        event.setState(State.PENDING);
        event.setViews(0L);
        event.setConfirmedRequests(0);

        Location location = Location.builder()
                .lon(newEventDto.getLocation().getLon())
                .lat(newEventDto.getLocation().getLat())
                .build();

        Location savedLocation = locationRepository.save(location);
        event.setLocation(savedLocation);

        return toEventFullDto(eventRepository.save(event));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEventsByUser(Long userId, int from, int size) {
        log.info("В сервисе получаем события");
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }
        Pageable pageable = PageRequest.of(from / size, size);
        return eventRepository.findAllByInitiatorId(userId, pageable).stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventByUser(Long userId, Long eventId) {
        log.info("В сервисе получаем событие");
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }
        return toEventFullDto(eventRepository.findByInitiatorIdAndId(userId, eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено")));
    }

    @Override
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        log.info("В сервисе обновляем событие");
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }

        Event oldEvent = eventRepository.findByInitiatorIdAndId(userId, eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));

        if (oldEvent.getState() == PUBLISHED) {
            throw new ConflictException("Нельзя редактировать событие в статусе 'PUBLISHED'");
        }

        if (updateEventUserRequest.getStateAction() == CANCEL_REVIEW) {
            oldEvent.setState(CANCELED);
        }
        if (updateEventUserRequest.getDescription() != null) {
            oldEvent.setDescription(updateEventUserRequest.getDescription());
        }
        if (updateEventUserRequest.getAnnotation() != null) {
            oldEvent.setAnnotation(updateEventUserRequest.getAnnotation());
        }
        if (updateEventUserRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateEventUserRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория не найдена"));
            oldEvent.setCategory(category);
        }
        if (updateEventUserRequest.getLocation() != null) {
            Location location = Location.builder()
                    .lon(updateEventUserRequest.getLocation().getLon())
                    .lat(updateEventUserRequest.getLocation().getLat())
                    .build();
            Location savedLocation = locationRepository.save(location);
            oldEvent.setLocation(savedLocation);
        }
        if (updateEventUserRequest.isPaid()) {
            oldEvent.setPaid(true);
        }
        if (updateEventUserRequest.getParticipantLimit() != null) {
            oldEvent.setParticipantLimit(updateEventUserRequest.getParticipantLimit());
        }
        if (!updateEventUserRequest.isRequestModeration()) {
            oldEvent.setRequestModeration(false);
        }
        if (updateEventUserRequest.getEventDate() != null) {
            LocalDateTime nowPlusTwoHours = LocalDateTime.now().plusHours(2);
            if (updateEventUserRequest.getEventDate().isBefore(nowPlusTwoHours)) {
                throw new ConflictException("Дата и время события должны быть не раньше, " +
                        "чем через два часа от текущего момента.");
            }
            oldEvent.setEventDate(updateEventUserRequest.getEventDate());
        }
        if (updateEventUserRequest.getTitle() != null) {
            oldEvent.setTitle(updateEventUserRequest.getTitle());
        }
        return toEventFullDto(eventRepository.save(oldEvent));
    }

    @Transactional(readOnly = true)
    @Override
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        log.info("В сервисе получаем запросы на участие события");
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }
        Event event = eventRepository.findByInitiatorIdAndId(userId, eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено или вам не пренадлежит"));

        List<Request> requests = requestRepository.findByEvent(eventId);

        return requests.stream().map(RequestMapper::toParticipationRequestDto).collect(Collectors.toList());
    }

    @Override
    public EventFullDto updateStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest eventRequestStatus) {
        log.info("В сервисе обновляем статус запроса");
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }
        Event event = eventRepository.findByInitiatorIdAndId(userId, eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено или вам не пренадлежит"));

        if (event.getConfirmedRequests() > event.getParticipantLimit() + eventRequestStatus.getRequestIds().size()) {
            throw new ConflictException("Нельзя подтвердить заявку, если уже достигнут лимит по заявкам на данное событие");
        }

        List<Long> requestIds = eventRequestStatus.getRequestIds();
        List<Request> requests = requestRepository.findAllByIdInAndEvent(requestIds, eventId);

        for (Request request : requests) {
            if (request.getStatus() != PENDING) {
                throw new ConflictException("Статус можно изменить только у заявок, находящихся в состоянии ожидания");
            }
        }

        List<Request> requestsWithStatus = requests.stream()
                .peek(request -> request.setStatus(eventRequestStatus.getStatus()))
                .toList();
        requestRepository.saveAll(requestsWithStatus);

        int ConfirmedRequests = event.getConfirmedRequests() + requestIds.size();

        event.setConfirmedRequests(ConfirmedRequests);

        return toEventFullDto(eventRepository.save(event));
    }

    @Override
    public EventFullDto getEvent(Long id, HttpServletRequest request) {
        log.info("В сервисе получаем событие");
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));

        if (event.getState() != PUBLISHED) {
            throw new NotFoundException("Событие ещё не опубликовано");
        }

        addStatistic(request);

        String[] uri = new String[]{String.format("/events/%s", event.getId())};
        ResponseEntity<Object> response = statClient.getStatistics(event.getCreatedOn(), LocalDateTime.now(), uri, false);

        List<ViewStats> viewStatsList = objectMapper.convertValue(response.getBody(), new TypeReference<List<ViewStats>>() {});

        long views = viewStatsList.stream()
                .filter(viewStats -> viewStats.getUri().equals(uri[0]))
                .mapToLong(ViewStats::getHits)
                .sum();

        EventFullDto eventFullDto = toEventFullDto(event);
        eventFullDto.setViews(views);

        return eventFullDto;
    }

    @Override
    public List<EventShortDto> getEvents(String text, List<Long> categories, Boolean paid,
                                         LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                         Boolean onlyAvailable, String sort, int from,
                                         int size, HttpServletRequest request) {
        log.info("В сервисе получаем события");

        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        } else {
            if (LocalDateTime.now().isAfter(rangeStart)) {
                rangeStart = LocalDateTime.now();
            }
        }
        if (rangeEnd == null) {
            rangeEnd = LocalDateTime.now().plusYears(100);
        }
        if (rangeStart.isAfter(rangeEnd)) {
            throw new ConflictException("Дата окончания не может быть раньше даты начала");
        }

        addStatistic(request);

        Pageable pageable = PageRequest.of(from / size, size);

        if (text != null) {
            text = text.toLowerCase();
        }

        List<Event> events = eventRepository.findAllPublishedWithFilters(text, categories, paid, rangeStart,
                rangeEnd, onlyAvailable, sort, pageable);

        List<EventShortDto> eventShortDtos = events.stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());

        for (EventShortDto eventShortDto : eventShortDtos) {
            String[] uri = new String[]{String.format("/events/%s", eventShortDto.getId())};
            ResponseEntity<Object> response = statClient.getStatistics(rangeStart, rangeEnd, uri, false);

            List<ViewStats> viewStatsList = objectMapper.convertValue(response.getBody(), new TypeReference<List<ViewStats>>() {});

            long views = viewStatsList.stream()
                    .filter(viewStats -> viewStats.getUri().equals(uri[0]))
                    .mapToLong(ViewStats::getHits)
                    .sum();

            eventShortDto.setViews(views);
        }

        return eventShortDtos;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getEventsForAdmin(List<Long> users, List<String> states,
                                         List<Long> categories, LocalDateTime rangeStart,
                                         LocalDateTime rangeEnd, int from, int size) {
        log.info("В сервисе получаем события");

        Pageable pageable = PageRequest.of(from / size, size);
        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }
        if (rangeEnd == null) {
            rangeEnd = LocalDateTime.now().plusYears(100);
        }

        List<Event> events = eventRepository.findAllByAdminFilters(users, states, categories, rangeStart, rangeEnd, pageable);

        return events.stream()
                .map(EventMapper::toEventFullDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto updateEventForAdmin(Long eventId, UpdateEventAdminRequest request) {
        log.info("В сервисе обновляем событие");

        Event oldEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));

        if (!oldEvent.getState().equals(State.PENDING)) {
            throw new ConflictException("Событие можно публиковать, только если оно в состоянии ожидания публикации");
        }

        if (request.getEventDate() != null) {
            LocalDateTime eventDate = request.getEventDate();
            if (eventDate.isBefore(LocalDateTime.now().plusHours(1))) {
                throw new ConflictException("Дата начала изменяемого события должна быть не ранее чем за час от даты публикации");
            }
            oldEvent.setEventDate(eventDate);
        }

        if (request.getStateAction() != null) {
            if (request.getStateAction() == PUBLISH_EVENT) {
                oldEvent.setState(State.PUBLISHED);
                oldEvent.setPublishedOn(LocalDateTime.now());
            } else if (request.getStateAction() == REJECT_EVENT) {
                if (oldEvent.getState().equals(State.PUBLISHED)) {
                    throw new ConflictException("Событие можно отклонить, только если оно еще не опубликовано");
                }
                oldEvent.setState(State.CANCELED);
            }
        }

        if (request.getAnnotation() != null) {
            oldEvent.setAnnotation(request.getAnnotation());
        }
        if (request.getParticipantLimit() != null) {
            oldEvent.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.getCategory() != null) {
            Category category = categoryRepository.findById(request.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория не найдена"));
            oldEvent.setCategory(category);
        }
        if (request.getDescription() != null) {
            oldEvent.setDescription(request.getDescription());
        }
        if (request.getLocation() != null) {
            Location location = Location.builder()
                    .lon(request.getLocation().getLon())
                    .lat(request.getLocation().getLat())
                    .build();

            Location savedLocation = locationRepository.save(location);
            oldEvent.setLocation(savedLocation);
        }
        if (request.isPaid()) {
            oldEvent.setPaid(true);
        }
        if (!request.isRequestModeration()) {
            oldEvent.setRequestModeration(false);
        }
        if (request.getTitle() != null) {
            oldEvent.setTitle(request.getTitle());
        }

        return toEventFullDto(eventRepository.save(oldEvent));
    }

    private void addStatistic(HttpServletRequest request) {
        statClient.saveHit(HitDto.builder()
                .app("ewm-service")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build());
    }
}