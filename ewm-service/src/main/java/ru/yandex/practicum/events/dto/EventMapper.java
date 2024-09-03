package ru.yandex.practicum.events.dto;

import ru.yandex.practicum.caregories.dto.CategoryDto;
import ru.yandex.practicum.events.Event;
import ru.yandex.practicum.users.dto.UserShortDto;

import static ru.yandex.practicum.caregories.dto.CategoryMapper.toCategory;
import static ru.yandex.practicum.caregories.dto.CategoryMapper.toCategoryDto;
import static ru.yandex.practicum.users.dto.UserMapper.toUser;
import static ru.yandex.practicum.users.dto.UserMapper.toUserShortDto;

public class EventMapper {
    private EventMapper() {
    }

    public static EventFullDto toEventFullDto(Event event) {
        CategoryDto categoryDto = toCategoryDto(event.getCategory());
        UserShortDto userShortDto = toUserShortDto(event.getInitiator());
        return EventFullDto.builder()
                .id(event.getId())
                .eventDate(event.getEventDate())
                .annotation(event.getAnnotation())
                .paid(event.isPaid())
                .category(categoryDto)
                .location(event.getLocation())
                .initiator(userShortDto)
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn())
                .confirmedRequests(event.getConfirmedRequests())
                .createdOn(event.getCreatedOn())
                .title(event.getTitle())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .requestModeration(event.isRequestModeration())
                .state(event.getState())
                .views(event.getViews())
                .build();
    }

    public static Event toEvent(EventFullDto eventFullDto) {
        return Event.builder()
                .id(eventFullDto.getId())
                .eventDate(eventFullDto.getEventDate())
                .annotation(eventFullDto.getAnnotation())
                .paid(eventFullDto.isPaid())
                .category(toCategory(eventFullDto.getCategory()))
                .location(eventFullDto.getLocation())
                .initiator(toUser(eventFullDto.getInitiator()))
                .participantLimit(eventFullDto.getParticipantLimit())
                .publishedOn(eventFullDto.getPublishedOn())
                .confirmedRequests(eventFullDto.getConfirmedRequests())
                .createdOn(eventFullDto.getCreatedOn())
                .description(eventFullDto.getDescription())
                .eventDate(eventFullDto.getEventDate())
                .title(eventFullDto.getTitle())
                .requestModeration(eventFullDto.isRequestModeration())
                .state(eventFullDto.getState())
                .views(eventFullDto.getViews())
                .build();
    }

    public static Event toEvent(NewEventDto newEventDto) {
        return Event.builder()
                .eventDate(newEventDto.getEventDate())
                .annotation(newEventDto.getAnnotation())
                .paid(newEventDto.isPaid())
                .title(newEventDto.getTitle())
                .location(newEventDto.getLocation())
                .participantLimit(newEventDto.getParticipantLimit())
                .description(newEventDto.getDescription())
                .eventDate(newEventDto.getEventDate())
                .requestModeration(newEventDto.isRequestModeration())
                .build();
    }

    public static EventShortDto toEventShortDto(Event event) {
        CategoryDto categoryDto = toCategoryDto(event.getCategory());
        UserShortDto userShortDto = toUserShortDto(event.getInitiator());
        return EventShortDto.builder()
                .id(event.getId())
                .eventDate(event.getEventDate())
                .annotation(event.getAnnotation())
                .paid(event.isPaid())
                .category(categoryDto)
                .title(event.getTitle())
                .initiator(userShortDto)
                .confirmedRequests(event.getConfirmedRequests())
                .eventDate(event.getEventDate())
                .views(event.getViews())
                .build();
    }
}