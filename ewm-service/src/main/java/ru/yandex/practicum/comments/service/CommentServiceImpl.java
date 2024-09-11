package ru.yandex.practicum.comments.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.comments.Comment;
import ru.yandex.practicum.comments.CommentRepository;
import ru.yandex.practicum.comments.dto.CommentDto;
import ru.yandex.practicum.comments.dto.CommentMapper;
import ru.yandex.practicum.comments.dto.NewCommentDto;
import ru.yandex.practicum.comments.dto.UpdateCommentDto;
import ru.yandex.practicum.events.Event;
import ru.yandex.practicum.events.EventRepository;
import ru.yandex.practicum.exception.ConflictDataException;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.users.User;
import ru.yandex.practicum.users.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static ru.yandex.practicum.comments.dto.CommentMapper.toComment;
import static ru.yandex.practicum.comments.dto.CommentMapper.toCommentDto;
import static ru.yandex.practicum.events.State.PUBLISHED;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {

    private final EventRepository eventRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Override
    public CommentDto createComment(NewCommentDto newCommentDto, Long userId) {
        log.info("В сервисе создаем комментарий");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Event event = eventRepository.findById(newCommentDto.getEventId())
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));

        if (event.getState() != PUBLISHED) {
            throw new ConflictDataException("Событие еще не опубликовано - комментировать нельзя");
        }
        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictDataException("Нельзя оставлять комментарии под событием, инициатором которого вы являетесь");
        }

        LocalDateTime createOn = LocalDateTime.now();

        Comment comment = toComment(newCommentDto, user, event, createOn);

        return toCommentDto(commentRepository.save(comment));
    }

    @Override
    public void deleteComment(Long comId, Long userId) {
        log.info("В сервисе удаляем комментарий");

        Comment comment = commentRepository.findById(comId)
                .orElseThrow(() -> new NotFoundException("Комментарий не найден"));

        if (!comment.getCreator().getId().equals(userId) && !comment.getEvent().getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Вы не являетесь создателем комментария или создателем события");
        }

        commentRepository.deleteById(comId);
    }

    @Override
    public CommentDto updateComment(UpdateCommentDto updateCommentDto, Long userId) {
        log.info("В сервисе обновляем комментарий");

        Comment oldComment = commentRepository.findByCreatorIdAndId(userId, updateCommentDto.getId())
                .orElseThrow(() -> new NotFoundException("Комментарий не найден"));

        oldComment.setText(updateCommentDto.getText());
        oldComment.setModified(true);
        oldComment.setChangedOn(LocalDateTime.now());

        return toCommentDto(commentRepository.save(oldComment));
    }

    @Override
    public List<CommentDto> getCommentsForEvent(int from, int size, Long eventId) {
        log.info("В сервисе получаем комментарии");

        Pageable pageable = PageRequest.of(from / size, size);

        List<Comment> comments = commentRepository.findAllByEventId(eventId, pageable);

        return comments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteCommentForAdmin(Long comId) {
        log.info("В сервисе удаляем комментарий для админа");

        if (!commentRepository.existsById(comId)) {
            throw new NotFoundException("Комментарий не найден");
        }
        commentRepository.deleteById(comId);
    }
}