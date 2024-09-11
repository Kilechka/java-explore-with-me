package ru.yandex.practicum.comments.dto;

import ru.yandex.practicum.comments.Comment;
import ru.yandex.practicum.events.Event;
import ru.yandex.practicum.users.User;

import java.time.LocalDateTime;

public class CommentMapper {

    private CommentMapper() {
    }

    public static Comment toComment(NewCommentDto newCommentDto, User user, Event event, LocalDateTime localDateTime) {
        return Comment.builder()
                .text(newCommentDto.getText())
                .event(event)
                .creator(user)
                .changedOn(localDateTime)
                .isModified(false)
                .build();
    }

    public static CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .text(comment.getText())
                .isModified(comment.isModified())
                .eventId(comment.getEvent().getId())
                .changedOn(comment.getChangedOn())
                .id(comment.getId())
                .creatorId(comment.getCreator().getId())
                .build();
    }


}