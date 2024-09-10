package ru.yandex.practicum.compilations.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.compilations.Compilation;
import ru.yandex.practicum.compilations.CompilationRepository;
import ru.yandex.practicum.compilations.dto.CompilationDto;
import ru.yandex.practicum.compilations.dto.CompilationMapper;
import ru.yandex.practicum.compilations.dto.NewCompilationDto;
import ru.yandex.practicum.compilations.dto.UpdateCompilationRequest;
import ru.yandex.practicum.events.Event;
import ru.yandex.practicum.events.EventRepository;
import ru.yandex.practicum.exception.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

import static ru.yandex.practicum.compilations.dto.CompilationMapper.toCompilationDto;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Override
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        log.info("В сервисе создаем подборку");

        List<Long> eventIds = newCompilationDto.getEvents();
        List<Event> events = eventRepository.findAllById(eventIds);

        if (events.size() != eventIds.size()) {
            List<Long> foundEventIds = events.stream().map(Event::getId).collect(Collectors.toList());
            eventIds.removeAll(foundEventIds);
            throw new NotFoundException("События с id " + eventIds + " не найдены");
        }

        Compilation compilation = Compilation.builder()
                .events(events)
                .pinned(newCompilationDto.isPinned())
                .title(newCompilationDto.getTitle())
                .build();

        return toCompilationDto(compilationRepository.save(compilation));
    }

    @Override
    public void deleteCompilation(Long compId) {
        log.info("В сервисе удаляем подборку");
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка не найдена"));

        compilationRepository.deleteById(compId);
    }

    @Override
    public CompilationDto updateCompilation(UpdateCompilationRequest updateCompilationRequest, Long compId) {
        log.info("В сервисе обновляем подборку");
        Compilation oldCompilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка не найдена"));

        if (updateCompilationRequest.getTitle() != null) {
            oldCompilation.setTitle(updateCompilationRequest.getTitle());
        }
        if (updateCompilationRequest.isPinned()) {
            oldCompilation.setPinned(updateCompilationRequest.isPinned());
        }
        if (updateCompilationRequest.getEvents() != null && !updateCompilationRequest.getEvents().isEmpty()) {
            List<Long> eventsIds = updateCompilationRequest.getEvents();

            List<Event> events = eventRepository.findAllById(eventsIds);
            oldCompilation.setEvents(events);
        }
        return toCompilationDto(compilationRepository.save(oldCompilation));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        log.info("В сервисе получаем подборки");
        Pageable pageable = PageRequest.of(from / size, size);

        List<Compilation> compilations;

        if (pinned == null) {
            compilations = compilationRepository.findAll(pageable).getContent();
        } else {
            compilations = compilationRepository.findAllByPinned(pinned, pageable);
        }

        return compilations.stream()
                .map(CompilationMapper::toCompilationDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getCompilationById(Long compId) {
        log.info("В сервисе получаем подборку");
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка не найдена"));

        return toCompilationDto(compilation);
    }
}