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
    import ru.yandex.practicum.comments.Comment;
    import ru.yandex.practicum.comments.CommentRepository;
    import ru.yandex.practicum.comments.dto.CommentDto;
    import ru.yandex.practicum.comments.dto.CommentMapper;
    import ru.yandex.practicum.events.Event;
    import ru.yandex.practicum.events.EventRepository;
    import ru.yandex.practicum.events.State;
    import ru.yandex.practicum.events.dto.*;
    import ru.yandex.practicum.events.locations.Location;
    import ru.yandex.practicum.events.locations.LocationRepository;
    import ru.yandex.practicum.exception.ConflictDataException;
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
    import java.util.ArrayList;
    import java.util.Comparator;
    import java.util.List;
    import java.util.Map;
    import java.util.stream.Collectors;

    import static ru.yandex.practicum.events.State.CANCELED;
    import static ru.yandex.practicum.events.State.PUBLISHED;
    import static ru.yandex.practicum.events.StateActionAdmin.PUBLISH_EVENT;
    import static ru.yandex.practicum.events.StateActionAdmin.REJECT_EVENT;
    import static ru.yandex.practicum.events.StateActionUser.CANCEL_REVIEW;
    import static ru.yandex.practicum.events.StateActionUser.SEND_TO_REVIEW;
    import static ru.yandex.practicum.events.dto.EventMapper.toEvent;
    import static ru.yandex.practicum.events.dto.EventMapper.toEventFullDto;
    import static ru.yandex.practicum.requests.Status.*;

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
    private final CommentRepository commentRepository;

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
                .orElseThrow(() -> new ConflictDataException("Событие не найдено или вам не пренадлежит"));

        if (oldEvent.getState() == PUBLISHED) {
            throw new ConflictDataException("Нельзя редактировать событие в статусе 'PUBLISHED'");
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
        if (updateEventUserRequest.getParticipantLimit() != 0) {
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
        if (updateEventUserRequest.getStateAction() == SEND_TO_REVIEW) {
            oldEvent.setState(State.PENDING);
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

        List<Request> requests = requestRepository.findByEventId(eventId);

        return requests.stream().map(RequestMapper::toParticipationRequestDto).collect(Collectors.toList());
    }

        @Override
        public EventRequestStatusUpdateResult updateStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest eventRequestStatus) {
            log.info("В сервисе обновляем статус запроса");

            if (!userRepository.existsById(userId)) {
                throw new NotFoundException("Пользователь не найден");
            }
            Event event = eventRepository.findByInitiatorIdAndId(userId, eventId)
                    .orElseThrow(() -> new ConflictDataException("Событие не найдено или вам не принадлежит"));

            int newRequestsCount = eventRequestStatus.getRequestIds().size();
            if (event.getConfirmedRequests() + newRequestsCount > event.getParticipantLimit()
                    && event.getParticipantLimit() != 0) {
                throw new ConflictDataException("Нельзя подтвердить заявку, если уже достигнут лимит по заявкам на данное событие");
            }
            if (!event.isRequestModeration() || event.getParticipantLimit() == 0) {
                throw new ConflictDataException("Не требуются подтверждения запросов");
            }

            List<Long> requestIds = eventRequestStatus.getRequestIds();
            List<Request> requests = requestRepository.findAllByIdInAndEventId(requestIds, eventId);

            List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
            List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();

            int confirmedCount = 0;

            for (Request request : requests) {
                if (request.getStatus() != PENDING) {
                    throw new ConflictDataException("Статус можно изменить только у заявок, находящихся в состоянии ожидания");
                }
                if (eventRequestStatus.getStatus() == CONFIRMED) {
                    if (confirmedCount < event.getParticipantLimit()) {
                        request.setStatus(CONFIRMED);
                        confirmedRequests.add(RequestMapper.toParticipationRequestDto(request));
                        confirmedCount++;
                    } else {
                        request.setStatus(REJECTED);
                        rejectedRequests.add(RequestMapper.toParticipationRequestDto(request));
                    }
                } else {
                    request.setStatus(REJECTED);
                    rejectedRequests.add(RequestMapper.toParticipationRequestDto(request));
                }
            }

            event.setConfirmedRequests(event.getConfirmedRequests() + confirmedCount);

            requestRepository.saveAll(requests);
            eventRepository.save(event);

            return EventRequestStatusUpdateResult.builder()
                    .confirmedRequests(confirmedRequests)
                    .rejectedRequests(rejectedRequests)
                    .build();
        }

        public EventFullDto getEvent(Long id, HttpServletRequest request) {
            log.info("В сервисе получаем событие");
            Event event = eventRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Событие не найдено"));

            if (event.getState() != PUBLISHED) {
                throw new NotFoundException("Событие ещё не опубликовано");
            }

            addStatistic(request);

            List<CommentDto> comments = commentRepository.findAllByEventId(id).stream()
                    .map(CommentMapper::toCommentDto)
                    .collect(Collectors.toList());

            String uri = request.getRequestURI();

            ResponseEntity<Object> response = statClient.getStatistics(LocalDateTime.now().minusYears(100),
                    LocalDateTime.now(), new String[]{uri}, true);
            List<ViewStats> viewStatsList = objectMapper.convertValue(response.getBody(), new TypeReference<>() {
            });

            Map<Object, Integer> viewStatsMap = viewStatsList.stream()
                    .filter(statsDto -> statsDto.getUri().startsWith("/events/"))
                    .collect(Collectors.toMap(
                            statsDto -> Long.parseLong(statsDto.getUri().substring("/events/".length())),
                            ViewStats::getHits));

            Integer views = viewStatsMap.getOrDefault(event.getId(), 0);

            EventFullDto eventFullDto = toEventFullDto(event);
            eventFullDto.setViews(views);
            eventFullDto.setComments(comments);

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

            List<Event> events;
            if (text != null) {
                text = text.toLowerCase();
                events = eventRepository.findAllPublishedWithFiltersWithText(text, categories, paid, rangeStart,
                        rangeEnd, onlyAvailable, pageable);
            } else {
                events = eventRepository.findAllPublishedWithFiltersWithoutText(categories, paid, rangeStart,
                        rangeEnd, onlyAvailable, pageable);
            }

            List<Long> eventsIds = events.stream().map(Event::getId).toList();

            List<Comment> comments = commentRepository.findAllByEventIdIn(eventsIds);

            List<EventShortDto> eventShortDtos = events.stream()
                    .map(EventMapper::toEventShortDto)
                    .collect(Collectors.toList());

            String[] uris = eventShortDtos.stream()
                    .map(eventShortDto -> String.format("/events/%s", eventShortDto.getId()))
                    .toArray(String[]::new);

            ResponseEntity<Object> response = statClient.getStatistics(rangeStart, rangeEnd, uris, false);

            List<ViewStats> viewStatsList = objectMapper.convertValue(response.getBody(), new TypeReference<List<ViewStats>>() {});

            for (EventShortDto eventShortDto : eventShortDtos) {
                String uri = String.format("/events/%s", eventShortDto.getId());
                long views = viewStatsList.stream()
                        .filter(viewStats -> viewStats.getUri().equals(uri))
                        .mapToLong(ViewStats::getHits)
                        .sum();
                List<CommentDto> commentDtos = comments.stream()
                        .filter(comment -> eventShortDto.getId().equals(comment.getEvent().getId()))
                        .map(CommentMapper::toCommentDto)
                        .toList();

                eventShortDto.setViews(views);
                eventShortDto.setComments(commentDtos);
            }

            if ("VIEWS".equals(sort)) {
                eventShortDtos.sort((e1, e2) -> Long.compare(e2.getViews(), e1.getViews()));
            } else if ("EVENT_DATE".equals(sort)) {
                eventShortDtos.sort(Comparator.comparing(EventShortDto::getEventDate));
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
            throw new ConflictDataException("Событие можно публиковать, только если оно в состоянии ожидания публикации");
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
                    throw new ConflictDataException("Событие можно отклонить, только если оно еще не опубликовано");
                }
                oldEvent.setState(State.CANCELED);
            }
        }

        if (request.getAnnotation() != null) {
            oldEvent.setAnnotation(request.getAnnotation());
        }
        if (request.getParticipantLimit() != 0) {
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