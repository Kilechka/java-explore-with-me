package ru.yandex.practicum.comments.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.comments.dto.CommentDto;
import ru.yandex.practicum.comments.dto.NewCommentDto;

import java.util.List;

public interface CommentService {

    CommentDto createComment(NewCommentDto newCommentDto, Long eventId, Long userId);

    void deleteComment(Long eventId, Long userId);

    CommentDto updateComment(NewCommentDto newCommentDto, Long comId, Long userId);

    List<CommentDto> getCommentsForEvent(int from, int size, Long eventId);

    void deleteCommentForAdmin(Long comId);
}