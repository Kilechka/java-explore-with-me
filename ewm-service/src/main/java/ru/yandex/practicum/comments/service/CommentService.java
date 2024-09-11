package ru.yandex.practicum.comments.service;

import ru.yandex.practicum.comments.dto.CommentDto;
import ru.yandex.practicum.comments.dto.NewCommentDto;
import ru.yandex.practicum.comments.dto.UpdateCommentDto;

import java.util.List;

public interface CommentService {

    CommentDto createComment(NewCommentDto newCommentDto, Long userId);

    void deleteComment(Long eventId, Long userId);

    CommentDto updateComment(UpdateCommentDto updateCommentDto, Long userId);

    List<CommentDto> getCommentsForEvent(int from, int size, Long eventId);

    void deleteCommentForAdmin(Long comId);
}