package ru.yandex.practicum.compilations.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.compilations.Compilation;
import ru.yandex.practicum.compilations.CompilationRepository;
import ru.yandex.practicum.compilations.comilationsEvents.CompilationEvent;
import ru.yandex.practicum.compilations.comilationsEvents.CompilationEventRepository;
import ru.yandex.practicum.compilations.dto.CompilationDto;
import ru.yandex.practicum.compilations.dto.CompilationMapper;
import ru.yandex.practicum.compilations.dto.NewCompilationDto;
import ru.yandex.practicum.compilations.dto.UpdateCompilationRequest;
import ru.yandex.practicum.events.Event;
import ru.yandex.practicum.events.EventRepository;
import ru.yandex.practicum.exception.ConflictDataException;
import ru.yandex.practicum.exception.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

import static ru.yandex.practicum.compilations.dto.CompilationMapper.toCompilation;
import static ru.yandex.practicum.compilations.dto.CompilationMapper.toCompilationDto;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationEventRepository compilationEventRepository;

    @Override
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        log.info("В сервисе создаем подборку");

        if (compilationRepository.existsByTitle(newCompilationDto.getTitle())) {
            throw new ConflictDataException("Данное название уже используется");
        }

        Compilation compilation = toCompilation(newCompilationDto);

        if (newCompilationDto.getEvents() != null) {
            List<Long> eventsIds = newCompilationDto.getEvents();
            List<Event> events = eventRepository.findAllById(eventsIds);
            compilation.setEvents(events);
            Compilation savedCompilation = compilationRepository.save(compilation);
            List<CompilationEvent> compilationEvents = eventsIds.stream()
                    .map(eventId -> new CompilationEvent(savedCompilation.getId(), eventId))
                    .toList();
            compilationEventRepository.saveAll(compilationEvents);
            return toCompilationDto(savedCompilation);
        } else {
            return toCompilationDto(compilationRepository.save(compilation));
        }
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
        if (oldCompilation.getTitle().equals(updateCompilationRequest.getTitle())) {
            throw new ConflictDataException("Данное название уже установлено");
        }

        if (updateCompilationRequest.getTitle() != null) {
            oldCompilation.setTitle(updateCompilationRequest.getTitle());
        }
        if (updateCompilationRequest.isPinned()) {
            oldCompilation.setPinned(true);
        }
        if (!updateCompilationRequest.getEvents().isEmpty()) {
            List<Long> eventsIds = updateCompilationRequest.getEvents();

            List<Event> events = eventRepository.findAllById(eventsIds);
            oldCompilation.setEvents(events);

            compilationEventRepository.deleteAllByCompilationId(compId);

            List<CompilationEvent> compilationEvents = eventsIds.stream()
                    .map(eventId -> new CompilationEvent(compId, eventId))
                    .toList();

            compilationEventRepository.saveAll(compilationEvents);
        }
        return toCompilationDto(compilationRepository.save(oldCompilation));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getCompilations(boolean pinned, int from, int size) {
        log.info("В сервисе получаем подборки");
        Pageable pageable = PageRequest.of(from / size, size);

        List<Compilation> compilations = compilationRepository.findAllByPinnedWithEvents(pinned, pageable);

        return compilations.stream()
                .map(CompilationMapper::toCompilationDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getCompilationById(Long compId) {
        log.info("В сервисе получаем подборку");
        Compilation compilation = compilationRepository.findWithEvents(compId)
                .orElseThrow(() -> new NotFoundException("Подборка не найдена"));

        return toCompilationDto(compilation);
    }
}